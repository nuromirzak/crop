package com.github.omirzak;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.omirzak.config.DependencyFactory;
import com.github.omirzak.dto.CropRequest;
import com.github.omirzak.dto.CropResponse;
import com.github.omirzak.dto.FaceCoordinate;
import com.github.omirzak.dto.PhotoSizeDTO;
import com.github.omirzak.service.ImageService;
import com.github.omirzak.service.PhotoSizeRepository;
import com.github.omirzak.service.RekognitionService;
import com.github.omirzak.util.AspectRatioUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.JSONInput;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class CropLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = Logger.getLogger(CropLambda.class.getName());

    private final S3Client s3Client;
    private final RekognitionService rekognitionService;
    private final String bucketName =
            Objects.requireNonNull(System.getenv("BUCKET_NAME"), "BUCKET_NAME is required");
    private final S3Presigner s3Presigner;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;
    private final PhotoSizeRepository photoSizeRepository;

    public CropLambda() {
        s3Client = DependencyFactory.s3Client();
        rekognitionService = DependencyFactory.rekognitionService();
        s3Presigner = DependencyFactory.s3Presigner();
        objectMapper = DependencyFactory.objectMapper();
        imageService = DependencyFactory.imageService();
        photoSizeRepository = DependencyFactory.photoSizeRepository();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("received: " + input);
        logger.info("BUCKET_NAME: " + bucketName);
        try {
            CropRequest cropRequest = objectMapper.readValue(input.getBody(), CropRequest.class);
            final String imageUrl = cropRequest.getImageUrl();
            final Integer id = cropRequest.getId();

            Optional<PhotoSizeDTO> photoSizeDTOOptional = photoSizeRepository.findById(id);

            if (photoSizeDTOOptional.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withBody("Photo size not found")
                        .withStatusCode(404);
            }

            List<FaceCoordinate> faceCoordinate = rekognitionService.detectFaces(imageUrl);
            PhotoSizeDTO photoSizeDTO = photoSizeDTOOptional.get();

            BufferedImage originalImage = imageService.getImageFromUrl(imageUrl);
            BufferedImage resultImage = imageService.cropImage(originalImage, photoSizeDTO, faceCoordinate);

            String presignedUrl = uploadAndGetPresignedUrl(resultImage, photoSizeDTO);

            return createResponse(new CropResponse(presignedUrl), 200);
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            return createResponse(Map.of("message", e.getMessage()), 500);
        }
    }

    private APIGatewayProxyResponseEvent createResponse(Object body, int statusCode) {
        try {
            String bodyString = objectMapper.writeValueAsString(body);
            return new APIGatewayProxyResponseEvent()
                    .withBody(bodyString)
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*",
                            "Access-Control-Allow-Headers", "*",
                            "Access-Control-Allow-Methods", "OPTIONS,POST,GET"
                    ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadAndGetPresignedUrl(BufferedImage image, PhotoSizeDTO photoSizeDTO) throws IOException {
        byte[] imageBytes = convertImageToBytes(image, photoSizeDTO.format());
        String key = "cropped/%s_%d.%s".formatted(photoSizeDTO.constructFileName(), System.currentTimeMillis(), photoSizeDTO.format());

        String contentType = switch (photoSizeDTO.format().toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

        logger.info("Image uploaded to S3 with key: " + key);

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(b -> b.bucket(bucketName).key(key))
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

        return presignedRequest.url().toString();
    }

    private byte[] convertImageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}

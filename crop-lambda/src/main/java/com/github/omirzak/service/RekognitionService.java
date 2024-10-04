package com.github.omirzak.service;

import com.github.omirzak.dto.FaceCoordinate;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class RekognitionService {
    private final RekognitionClient rekognitionClient;
    private final ImageService imageService;

    public RekognitionService(RekognitionClient rekognitionClient, ImageService imageService) {
        this.rekognitionClient = rekognitionClient;
        this.imageService = imageService;
    }

    @NotNull
    public List<FaceCoordinate> detectFaces(String imageUrl) {
        try {
            SdkBytes imageBytes = SdkBytes.fromInputStream(URI.create(imageUrl).toURL().openStream());

            DetectFacesRequest request = DetectFacesRequest.builder()
                    .image(Image.builder().bytes(imageBytes).build())
                    .attributes(Attribute.ALL)
                    .build();

            DetectFacesResponse result = rekognitionClient.detectFaces(request);

            if (result.faceDetails().isEmpty()) {
                throw new RuntimeException("No faces detected");
            }

            return result.faceDetails().stream()
                    .map(faceDetail -> convertToAbsoluteCoordinates(faceDetail.boundingBox(), imageUrl))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FaceCoordinate convertToAbsoluteCoordinates(
            BoundingBox boundingBox,
            String imageUrl
    ) {
        BufferedImage image = imageService.getImageFromUrl(imageUrl);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int left = (int) (boundingBox.left() * imageWidth);
        int top = (int) (boundingBox.top() * imageHeight);
        int width = (int) (boundingBox.width() * imageWidth);
        int height = (int) (boundingBox.height() * imageHeight);

        return new FaceCoordinate(left, top, width, height);
    }
}

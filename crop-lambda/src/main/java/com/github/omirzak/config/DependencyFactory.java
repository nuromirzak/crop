package com.github.omirzak.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.omirzak.CropLambda;
import com.github.omirzak.service.ImageService;
import com.github.omirzak.service.ImageServiceImpl;
import com.github.omirzak.service.PhotoSizeRepository;
import com.github.omirzak.service.RekognitionService;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * The module containing all dependencies required by the {@link CropLambda}.
 */
public class DependencyFactory {
    private static S3Client s3Client;
    private static RekognitionClient rekognitionClient;
    private static S3Presigner s3Presigner;
    private static RekognitionService rekognitionService;
    private static ImageService imageService;
    private static ObjectMapper objectMapper;
    private static PhotoSizeRepository photoSizeRepository;

    private DependencyFactory() {
    }

    public static S3Client s3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .httpClientBuilder(ApacheHttpClient.builder())
                    .build();
        }
        return s3Client;
    }

    private static RekognitionClient rekognitionClient() {
        if (rekognitionClient == null) {
            rekognitionClient = RekognitionClient.builder()
                    .httpClientBuilder(ApacheHttpClient.builder())
                    .build();
        }
        return rekognitionClient;
    }

    public static S3Presigner s3Presigner() {
        if (s3Presigner == null) {
            s3Presigner = S3Presigner.builder()
                    .build();
        }
        return s3Presigner;
    }

    public static RekognitionService rekognitionService() {
        if (rekognitionService == null) {
            rekognitionService = new RekognitionService(rekognitionClient(), imageService());
        }
        return rekognitionService;
    }

    public static ImageService imageService() {
        if (imageService == null) {
            imageService = new ImageServiceImpl();
        }
        return imageService;
    }

    public static ObjectMapper objectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        }
        return objectMapper;
    }

    public static PhotoSizeRepository photoSizeRepository() {
        if (photoSizeRepository == null) {
            photoSizeRepository = new PhotoSizeRepository();
        }
        return photoSizeRepository;
    }
}

package com.github.omirzak.service;

import com.github.omirzak.config.DependencyFactory;
import com.github.omirzak.dto.FaceCoordinate;
import com.github.omirzak.dto.PhotoSizeDTO;
import com.github.omirzak.dto.Point;
import com.github.omirzak.util.AspectRatioUtil;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class ImageServiceImpl implements ImageService {
    private static final double ASPECT_RATIO_THRESHOLD = 0.01;

    @NotNull
    public BufferedImage getImageFromUrl(String imageUrl) {
        try {
            URL url = URI.create(imageUrl).toURL();
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the image from URL: " + imageUrl);
        }
    }

    @Override
    public BufferedImage cropImage(BufferedImage image, PhotoSizeDTO photoSizeDTO, List<FaceCoordinate> faceCoordinates) {
        BufferedImage croppedImage = cropImageAroundFace(image, faceCoordinates);
        double[] aspectRatio = AspectRatioUtil.calculateAspectRatio(
                photoSizeDTO.width(), photoSizeDTO.height()
        );
        double widthRatio = aspectRatio[0];
        double heightRatio = aspectRatio[1];
        BufferedImage aspectRatioCroppedImage = cropToAspectRatio(croppedImage, widthRatio, heightRatio);
        BufferedImage resizedImage = resizeImageWithAspectRatioCheck(aspectRatioCroppedImage, photoSizeDTO.width(), photoSizeDTO.height());
        return resizedImage;
    }

    private BufferedImage cropImageAroundFace(BufferedImage originalImage, List<FaceCoordinate> faceCoordinates) {
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();

        assertValidFaceCoordinates(originalImage, faceCoordinates);

        Point faceCenter = calculateCenterOfFaces(faceCoordinates);
        System.out.println("faceCenter = " + faceCenter);

        int minHalfWidth = Math.min(faceCenter.x(), imageWidth - faceCenter.x());
        int minHalfHeight = Math.min(faceCenter.y(), imageHeight - faceCenter.y());

        int cropWidth = minHalfWidth * 2;
        int cropHeight = minHalfHeight * 2;

        int cropX = faceCenter.x() - minHalfWidth;
        int cropY = faceCenter.y() - minHalfHeight;

        return originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);
    }

    private void assertValidFaceCoordinates(BufferedImage image, List<FaceCoordinate> faceCoordinates) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        for (FaceCoordinate face : faceCoordinates) {
            if (face.left() < 0 || face.top() < 0 ||
                    face.left() + face.width() > imageWidth ||
                    face.top() + face.height() > imageHeight) {
                throw new IllegalArgumentException("Invalid face coordinates: Face coordinates must be non-negative and within image dimensions.");
            }
        }
    }

    private Point calculateCenterOfFaces(List<FaceCoordinate> faceCoordinates) {
        if (faceCoordinates.isEmpty()) {
            throw new IllegalArgumentException("Face coordinates list is empty");
        }

        int leftMost = Integer.MAX_VALUE;
        int rightMost = Integer.MIN_VALUE;
        int topMost = Integer.MAX_VALUE;
        int bottomMost = Integer.MIN_VALUE;

        for (FaceCoordinate face : faceCoordinates) {
            leftMost = Math.min(leftMost, face.left());
            rightMost = Math.max(rightMost, face.left() + face.width());
            topMost = Math.min(topMost, face.top());
            bottomMost = Math.max(bottomMost, face.top() + face.height());
        }

        int centerX = (leftMost + rightMost) / 2;
        int centerY = (topMost + bottomMost) / 2;

        return new Point(centerX, centerY);
    }

    private BufferedImage cropToAspectRatio(BufferedImage original, double widthRatio, double heightRatio) {
        try {
            double aspectRatio = widthRatio / heightRatio;
            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            int croppedWidth, croppedHeight;
            if (originalWidth / aspectRatio <= originalHeight) {
                croppedWidth = originalWidth;
                croppedHeight = (int) (croppedWidth / aspectRatio);
            } else {
                croppedHeight = originalHeight;
                croppedWidth = (int) (croppedHeight * aspectRatio);
            }

            int x = (originalWidth - croppedWidth) / 2;
            int y = (originalHeight - croppedHeight) / 2;

            return original.getSubimage(x, y, croppedWidth, croppedHeight);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage resizeImageWithAspectRatioCheck(BufferedImage originalImage, int targetWidth, int targetHeight) {
        double originalRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        double aspectRatioDifference = Math.abs(originalRatio - targetRatio) / originalRatio;
        if (aspectRatioDifference > ASPECT_RATIO_THRESHOLD) {
            throw new IllegalArgumentException(
                    String.format("Aspect ratio mismatch: %.4f != %.4f (difference: %.2f%%)",
                            originalRatio, targetRatio, aspectRatioDifference * 100)
            );
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D g2d = resizedImage.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (originalImage.getColorModel().hasAlpha()) {
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            }

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }

        return resizedImage;
    }

    private static final Logger logger = Logger.getLogger(ImageServiceImpl.class.getName());
    private static final Path outputFolder = Path.of("output");

    public static void main(String[] args) throws IOException {
        logger.info("App started");

        final String[] images = {
//                "https://i.ytimg.com/vi/bi2rbFMnrRs/maxresdefault.jpg",
//                "https://cdn.britannica.com/35/238335-050-2CB2EB8A/Lionel-Messi-Argentina-Netherlands-World-Cup-Qatar-2022.jpg",
//                "https://tntmusic.ru/media/content/article/2018-06-01_10-44-34__c5b33628-6588-11e8-a216-171ef27544dd.jpg",
                "https://i.imgur.com/ARtmlni.jpeg",
        };

        Files.createDirectories(outputFolder);
        ImageServiceImpl imageService = (ImageServiceImpl) DependencyFactory.imageService();
        RekognitionService rekognitionService = DependencyFactory.rekognitionService();
        List<PhotoSizeDTO> photoSizes = DependencyFactory.photoSizeRepository().getAllPhotoSizes();


        for (String imageUrl : images) {
            String imageName1 = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            logger.info("Processing image: %s".formatted(imageUrl));
            List<FaceCoordinate> faceCoordinate = rekognitionService.detectFaces(imageUrl);
            logger.info("Face coordinates: %s".formatted(faceCoordinate));
            BufferedImage image = imageService.getImageFromUrl(imageUrl);
            BufferedImage croppedFace = imageService.cropImageAroundFace(image, faceCoordinate);
            saveImage(croppedFace, imageName1 + "-cropped-face");

            for (PhotoSizeDTO photoSizeDTO : photoSizes) {
                String imageName = "image_" + photoSizeDTO.width() + "x" + photoSizeDTO.height();
                double[] aspectRatio = AspectRatioUtil.calculateAspectRatio(
                        photoSizeDTO.width(), photoSizeDTO.height()
                );
                double widthRatio = aspectRatio[0];
                double heightRatio = aspectRatio[1];
                BufferedImage aspectRatioCroppedImage = imageService.cropToAspectRatio(croppedFace, widthRatio, heightRatio);
                saveImage(aspectRatioCroppedImage, imageName + "-cropped-image");
                BufferedImage resizedImage = imageService.resizeImageWithAspectRatioCheck(aspectRatioCroppedImage, photoSizeDTO.width(), photoSizeDTO.height());
                saveImage(resizedImage, imageName + "-resized-image");
            }
        }
    }

    private static void saveImage(BufferedImage image, String name) {
        try {
            ImageIO.write(image, "jpg", new FileOutputStream(String.format("%s/%s.jpg", outputFolder, name)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

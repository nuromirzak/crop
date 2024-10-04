package com.github.omirzak.service;

import com.github.omirzak.dto.FaceCoordinate;
import com.github.omirzak.dto.PhotoSizeDTO;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageService {
    @NotNull
    BufferedImage getImageFromUrl(String imageUrl);

    BufferedImage cropImage(BufferedImage image, PhotoSizeDTO photoSizeDTO, List<FaceCoordinate> faceCoordinates);
}

package com.github.omirzak.service;

import com.github.omirzak.dto.PhotoSizeDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhotoSizeRepository {
    private final List<PhotoSizeDTO> photoSizes;

    public PhotoSizeRepository() {
        photoSizes = new ArrayList<>();
        initializePhotoSizes();
    }

    private void initializePhotoSizes() {
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("AppleMusic").width(2400).height(2400).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Spotify").width(2400).height(2400).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Header").platform("Spotify").width(2660).height(1440).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("YandexMusic").width(1000).height(1000).sizeInBytes(400 * 1024).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Zvuk").width(1080).height(1080).sizeInBytes(300 * 1024).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava Web").platform("VKMusic").width(1820).height(458).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava Mobile").platform("VKMusic").width(1500).height(1120).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("YouTubeMusic").width(5120).height(2880).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Deezer").width(1200).height(1200).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("VK").width(2400).height(2400).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Header").platform("VK").width(1920).height(768).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Instagram").width(2400).height(2400).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Facebook").width(196).height(196).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Header").platform("Facebook").width(851).height(315).sizeInBytes(100 * 1024).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Ava").platform("Telegram").width(2400).height(2400).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Creative").platform("VK").width(1080).height(1080).format("jpg").build());
        photoSizes.add(new PhotoSizeDTO.Builder().type("Creative").platform("VK").width(900).height(600).format("jpg").build());
    }

    public List<PhotoSizeDTO> getAllPhotoSizes() {
        return Collections.unmodifiableList(photoSizes);
    }

    public Optional<PhotoSizeDTO> findById(Integer id) {
        if (id < 1 || id > photoSizes.size()) {
            return Optional.empty();
        }
        return Optional.of(photoSizes.get(id - 1));
    }
}

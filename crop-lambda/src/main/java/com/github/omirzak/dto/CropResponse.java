package com.github.omirzak.dto;

import java.util.Objects;

public class CropResponse {
    private String imageUrl;

    public CropResponse() {
    }

    public CropResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "CropResponse{" +
                "imageUrl='" + imageUrl + '\'' +
                '}';
    }
}

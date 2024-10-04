package com.github.omirzak.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CropRequest {
    private String imageUrl;
    private Integer id;

    public CropRequest() {
    }

    public CropRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonCreator
    public CropRequest(
            @JsonProperty(value = "imageUrl", required = true) String imageUrl,
            @JsonProperty(value = "id", required = true) Integer id
    ) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("imageUrl cannot be null or empty");
        }
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.imageUrl = imageUrl;
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CropRequest{" +
                "imageUrl='" + imageUrl + '\'' +
                ", id=" + id +
                '}';
    }
}

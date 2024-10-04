package com.github.omirzak.dto;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PhotoSizeDTO(
        String type,
        String platform,
        Integer width,
        Integer height,
        @Nullable Integer sizeInBytes,
        String format
) {
    public PhotoSizeDTO {
        if (type == null) throw new NullPointerException("Type must not be null");
        if (platform == null) throw new NullPointerException("Platform must not be null");
        if (width == null) throw new NullPointerException("Width must not be null");
        if (height == null) throw new NullPointerException("Height must not be null");
        if (format == null) throw new NullPointerException("Format must not be null");
    }

    public PhotoSizeDTO(Builder builder) {
        this(builder.type, builder.platform, builder.width, builder.height, builder.sizeInBytes, builder.format);
    }

    public String constructFileName() {
        return String.format("%s_%s_%dx%d", type, platform, width, height);
    }

    public static class Builder {
        private String type;
        private String platform;
        private Integer width;
        private Integer height;
        private Integer sizeInBytes;
        private String format;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder sizeInBytes(Integer sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public PhotoSizeDTO build() {
            return new PhotoSizeDTO(this);
        }
    }
}

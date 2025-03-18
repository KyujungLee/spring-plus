package org.example.expert.domain.image;

import lombok.Getter;

@Getter
public class ImageResponse {

    private final String imageURL;

    public ImageResponse(String imageURL) {
        this.imageURL = imageURL;
    }
}

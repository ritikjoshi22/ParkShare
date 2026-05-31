package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class ParkingImageDto {
    @SerializedName("id")
    private long id;

    @SerializedName("image_url")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }
}

package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class ReviewDto {
    @SerializedName("id")
    private long id;

    @SerializedName("rating")
    private int rating;

    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("user")
    private UserDto user;

    @SerializedName("created_at")
    private String createdAt;

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public UserDto getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

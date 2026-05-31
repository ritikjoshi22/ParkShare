package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class NotificationDto {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("is_read")
    private int read;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read == 1;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

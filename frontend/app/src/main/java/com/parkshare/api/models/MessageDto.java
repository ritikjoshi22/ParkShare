package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class MessageDto {
    @SerializedName("id")
    private long id;

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("receiver_id")
    private long receiverId;

    @SerializedName("booking_id")
    private Long bookingId;

    @SerializedName("message")
    private String message;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() {
        return id;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

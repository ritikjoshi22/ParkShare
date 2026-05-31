package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class SosRequestDto {
    @SerializedName("id")
    private long id;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("emergency_message")
    private String emergencyMessage;

    @SerializedName("status")
    private String status;

    @SerializedName("technician")
    private TechnicianDto technician;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getEmergencyMessage() {
        return emergencyMessage;
    }

    public String getStatus() {
        return status;
    }

    public TechnicianDto getTechnician() {
        return technician;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

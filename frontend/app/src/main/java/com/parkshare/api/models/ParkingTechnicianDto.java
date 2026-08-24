package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class ParkingTechnicianDto {
    @SerializedName("id")
    private long id;

    @SerializedName("parking_space_id")
    private long parkingSpaceId;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("alternate_phone")
    private String alternatePhone;

    @SerializedName("email")
    private String email;

    @SerializedName("specialization")
    private String specialization;

    @SerializedName("description")
    private String description;

    @SerializedName("availability_status")
    private String availabilityStatus;

    @SerializedName("is_primary")
    private boolean primary;

    @SerializedName("is_active")
    private boolean active;

    public long getId() {
        return id;
    }

    public long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getDescription() {
        return description;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isActive() {
        return active;
    }
}

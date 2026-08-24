package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ParkingSpaceDto {
    @SerializedName("id")
    private long id;

    @SerializedName("parking_name")
    private String parkingName;

    @SerializedName("description")
    private String description;

    @SerializedName("address")
    private String address;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("price_per_hour")
    private double pricePerHour;

    @SerializedName("total_slots")
    private long totalSlots;

    @SerializedName("available_slots")
    private long availableSlots;

    @SerializedName("vehicle_type")
    private String vehicleType;

    @SerializedName("owner_id")
    private long ownerId;

    @SerializedName("opening_time")
    private String openingTime;

    @SerializedName("closing_time")
    private String closingTime;

    @SerializedName("is_verified")
    private boolean verified;

    @SerializedName("is_active")
    private boolean active;

    @SerializedName("distance_km")
    private Double distanceKm;

    @SerializedName("images")
    private List<ParkingImageDto> images;

    @SerializedName("reviews_avg_rating")
    private Double reviewsAvgRating;

    @SerializedName("technicians")
    private List<ParkingTechnicianDto> technicians;

    public long getId() {
        return id;
    }

    public String getParkingName() {
        return parkingName;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public long getTotalSlots() {
        return totalSlots;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public List<ParkingImageDto> getImages() {
        return images;
    }

    public Double getReviewsAvgRating() {
        return reviewsAvgRating;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public List<ParkingTechnicianDto> getTechnicians() {
        return technicians;
    }
}

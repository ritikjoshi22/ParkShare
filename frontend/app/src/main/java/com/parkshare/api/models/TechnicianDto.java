package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TechnicianDto {
    @SerializedName("id")
    private long id;

    @SerializedName("specialization")
    private String specialization;

    @SerializedName("experience_years")
    private long experienceYears;

    @SerializedName("service_radius_km")
    private long serviceRadiusKm;

    @SerializedName("availability_status")
    private String availabilityStatus;

    @SerializedName("description")
    private String description;

    @SerializedName("hourly_rate")
    private Double hourlyRate;

    @SerializedName("user")
    private UserDto user;

    @SerializedName("services")
    private List<TechnicianServiceDto> services;

    @SerializedName("reviews_avg_rating")
    private Double reviewsAvgRating;

    public long getId() {
        return id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public String getDescription() {
        return description;
    }

    public UserDto getUser() {
        return user;
    }

    public List<TechnicianServiceDto> getServices() {
        return services;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public long getExperienceYears() {
        return experienceYears;
    }

    public long getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public Double getReviewsAvgRating() {
        return reviewsAvgRating;
    }
}

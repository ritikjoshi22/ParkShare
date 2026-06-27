package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class OwnerStatsDto {
    @SerializedName("total_parking_spaces")
    private long totalParkingSpaces;

    @SerializedName("active_bookings")
    private long activeBookings;

    @SerializedName("monthly_revenue")
    private double monthlyRevenue;

    @SerializedName("available_slots")
    private long availableSlots;

    @SerializedName("total_slots")
    private long totalSlots;

    @SerializedName("occupancy_rate")
    private double occupancyRate;

    @SerializedName("top_parking_name")
    private String topParkingName;

    @SerializedName("top_parking_bookings")
    private long topParkingBookings;

    public long getTotalParkingSpaces() {
        return totalParkingSpaces;
    }

    public long getActiveBookings() {
        return activeBookings;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public long getAvailableSlots() {
        return availableSlots;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public String getTopParkingName() {
        return topParkingName;
    }
}

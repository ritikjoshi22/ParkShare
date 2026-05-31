package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class OwnerStatsDto {
    @SerializedName("total_parking_spaces")
    private int totalParkingSpaces;

    @SerializedName("active_bookings")
    private int activeBookings;

    @SerializedName("monthly_revenue")
    private double monthlyRevenue;

    @SerializedName("available_slots")
    private int availableSlots;

    @SerializedName("total_slots")
    private int totalSlots;

    @SerializedName("occupancy_rate")
    private double occupancyRate;

    @SerializedName("top_parking_name")
    private String topParkingName;

    @SerializedName("top_parking_bookings")
    private int topParkingBookings;

    public int getTotalParkingSpaces() {
        return totalParkingSpaces;
    }

    public int getActiveBookings() {
        return activeBookings;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public String getTopParkingName() {
        return topParkingName;
    }
}

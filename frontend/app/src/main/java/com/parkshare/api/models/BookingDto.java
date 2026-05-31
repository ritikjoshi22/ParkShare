package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class BookingDto {
    @SerializedName("id")
    private long id;

    @SerializedName("parking_space_id")
    private long parkingSpaceId;

    @SerializedName("booking_date")
    private String bookingDate;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("total_hours")
    private double totalHours;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("booking_status")
    private String bookingStatus;

    @SerializedName("parking_space")
    private ParkingSpaceDto parkingSpace;

    public long getId() {
        return id;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public ParkingSpaceDto getParkingSpace() {
        return parkingSpace;
    }
}

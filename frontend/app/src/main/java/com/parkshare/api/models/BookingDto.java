package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class BookingDto {
    @SerializedName("id")
    private long id;

    @SerializedName("user_id")
    private long userId;

    @SerializedName("parking_space_id")
    private long parkingSpaceId;

    @SerializedName("check_in_status")
    private String checkInStatus;

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

    @SerializedName("qr_code")
    private String qrCode;

    @SerializedName("checked_in_at")
    private String checkedInAt;

    @SerializedName("checked_out_at")
    private String checkedOutAt;

    @SerializedName("overtime_fee")
    private double overtimeFee;

    @SerializedName("parking_slot_id")
    private Long parkingSlotId;

    @SerializedName("payment_status")
    private String paymentStatus;

    @SerializedName("amount_due")
    private double amountDue;

    @SerializedName("extended_minutes")
    private long extendedMinutes;

    @SerializedName("parking_slot")
    private ParkingSlotDto parkingSlot;

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

    public double getTotalHours() {
        return totalHours;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getCheckedInAt() {
        return checkedInAt;
    }

    public String getCheckedOutAt() {
        return checkedOutAt;
    }

    public double getOvertimeFee() {
        return overtimeFee;
    }

    public long getUserId() {
        return userId;
    }

    public long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public String getCheckInStatus() {
        return checkInStatus;
    }

    public ParkingSpaceDto getParkingSpace() {
        return parkingSpace;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public long getExtendedMinutes() {
        return extendedMinutes;
    }

    public ParkingSlotDto getParkingSlot() {
        return parkingSlot;
    }

    public Long getParkingSlotId() {
        return parkingSlotId;
    }
}

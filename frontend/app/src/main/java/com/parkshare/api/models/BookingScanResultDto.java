package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class BookingScanResultDto {
    @SerializedName("payment_required")
    private boolean paymentRequired;

    @SerializedName("amount_due")
    private double amountDue;

    @SerializedName("action")
    private String action;

    @SerializedName("message")
    private String message;

    @SerializedName("booking")
    private BookingDto booking;

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public BookingDto getBooking() {
        return booking;
    }

    public boolean isPaymentRequired() {
        return paymentRequired;
    }

    public double getAmountDue() {
        return amountDue;
    }
}

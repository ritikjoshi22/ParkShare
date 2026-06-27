package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookingQuoteDto {
    @SerializedName("total")
    private double total;

    @SerializedName("hours")
    private double hours;

    @SerializedName("daily_cap_applied")
    private boolean dailyCapApplied;

    @SerializedName("breakdown")
    private List<BreakdownItem> breakdown;

    public double getTotal() {
        return total;
    }

    public double getHours() {
        return hours;
    }

    public boolean isDailyCapApplied() {
        return dailyCapApplied;
    }

    public static class BreakdownItem {
        @SerializedName("hour")
        private long hour;

        @SerializedName("rate")
        private double rate;

        @SerializedName("amount")
        private double amount;

        public long getHour() {
            return hour;
        }

        public double getRate() {
            return rate;
        }
    }
}

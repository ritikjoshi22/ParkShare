package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class ExtensionOptionsDto {
    @SerializedName("can_extend")
    private boolean canExtend;

    @SerializedName("max_end_time")
    private String maxEndTime;

    @SerializedName("max_minutes")
    private int maxMinutes;

    @SerializedName("reason")
    private String reason;

    @SerializedName("next_booking_start")
    private String nextBookingStart;

    public boolean canExtend() {
        return canExtend;
    }

    public int getMaxMinutes() {
        return maxMinutes;
    }

    public String getReason() {
        return reason;
    }

    public String getMaxEndTime() {
        return maxEndTime;
    }

    public String getNextBookingStart() {
        return nextBookingStart;
    }
}

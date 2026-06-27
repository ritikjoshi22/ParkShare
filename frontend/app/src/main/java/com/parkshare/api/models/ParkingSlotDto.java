package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class ParkingSlotDto {
    @SerializedName("id")
    private long id;

    @SerializedName("slot_number")
    private long slotNumber;

    @SerializedName("label")
    private String label;

    @SerializedName("status")
    private String status;

    @SerializedName("display_status")
    private String displayStatus;

    public long getId() {
        return id;
    }

    public long getSlotNumber() {
        return slotNumber;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayStatus() {
        return displayStatus != null ? displayStatus : status;
    }
}

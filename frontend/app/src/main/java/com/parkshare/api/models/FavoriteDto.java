package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class FavoriteDto {
    @SerializedName("id")
    private long id;

    @SerializedName("parking_space_id")
    private long parkingSpaceId;

    @SerializedName("parking_space")
    private ParkingSpaceDto parkingSpace;

    public long getId() {
        return id;
    }

    public long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public ParkingSpaceDto getParkingSpace() {
        return parkingSpace;
    }
}

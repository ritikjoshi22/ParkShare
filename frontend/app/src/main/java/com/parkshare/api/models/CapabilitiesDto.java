package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class CapabilitiesDto {
    @SerializedName("driver")
    private boolean driver;

    @SerializedName("owner")
    private boolean owner;

    public boolean isDriver() {
        return driver;
    }

    public boolean isOwner() {
        return owner;
    }
}

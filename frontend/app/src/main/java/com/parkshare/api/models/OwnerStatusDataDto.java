package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class OwnerStatusDataDto {
    @SerializedName("capabilities")
    private CapabilitiesDto capabilities;

    @SerializedName("owner")
    private OwnerStatusDto owner;

    @SerializedName("profile")
    private OwnerProfileDto profile;

    public CapabilitiesDto getCapabilities() {
        return capabilities;
    }

    public OwnerStatusDto getOwner() {
        return owner;
    }

    public OwnerProfileDto getProfile() {
        return profile;
    }
}

package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class TechnicianServiceDto {
    @SerializedName("id")
    private long id;

    @SerializedName("service_name")
    private String serviceName;

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }
}

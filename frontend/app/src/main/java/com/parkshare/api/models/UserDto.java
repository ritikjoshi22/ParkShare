package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    @SerializedName("id")
    private long id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("profile_image")
    private String profileImage;

    @SerializedName("address")
    private String address;

    @SerializedName("technician")
    private TechnicianDto technician;

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getAddress() {
        return address;
    }

    public TechnicianDto getTechnician() {
        return technician;
    }
}

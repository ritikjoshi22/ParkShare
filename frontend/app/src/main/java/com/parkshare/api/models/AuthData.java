package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class AuthData {
    @SerializedName("user")
    private UserDto user;

    @SerializedName("token")
    private String token;

    @SerializedName("token_type")
    private String tokenType;

    public UserDto getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}

package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("meta")
    private PaginationMeta meta;

    @SerializedName("errors")
    private Map<String, String[]> errors;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public PaginationMeta getMeta() {
        return meta;
    }

    public Map<String, String[]> getErrors() {
        return errors;
    }
}

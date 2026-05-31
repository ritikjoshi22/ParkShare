package com.parkshare.frontend.utils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.parkshare.api.models.ApiResponse;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {
    }

    public static String parse(Response<?> response) {
        String fallback = "Request failed (" + response.code() + ")";
        ResponseBody body = response.errorBody();
        if (body == null) {
            return fallback;
        }
        try {
            String raw = body.string();
            ApiResponse<?> apiResponse = GSON.fromJson(raw, ApiResponse.class);
            if (apiResponse != null && apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
                String validation = formatErrors(apiResponse.getErrors());
                if (validation != null) {
                    return validation;
                }
                return apiResponse.getMessage();
            }
            com.google.gson.JsonObject json = GSON.fromJson(raw, com.google.gson.JsonObject.class);
            if (json != null && json.has("message") && !json.get("message").isJsonNull()) {
                return json.get("message").getAsString();
            }
        } catch (IOException | JsonSyntaxException ignored) {
        }
        return fallback;
    }

    @Nullable
    private static String formatErrors(@Nullable Map<String, String[]> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String[]> entry : errors.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > 0) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append(entry.getValue()[0]);
            }
        }
        return builder.length() > 0 ? builder.toString() : null;
    }
}

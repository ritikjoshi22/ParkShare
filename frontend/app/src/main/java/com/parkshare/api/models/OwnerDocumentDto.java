package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class OwnerDocumentDto {
    @SerializedName("id")
    private long id;

    @SerializedName("document_type")
    private String documentType;

    @SerializedName("original_name")
    private String originalName;

    @SerializedName("status")
    private String status;

    public long getId() {
        return id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getOriginalName() {
        return originalName;
    }
}

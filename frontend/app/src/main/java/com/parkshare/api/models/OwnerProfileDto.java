package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class OwnerProfileDto {
    @SerializedName("id")
    private long id;

    @SerializedName("status")
    private String status;

    @SerializedName("current_step")
    private int currentStep;

    @SerializedName("step_data")
    private Map<String, Map<String, Object>> stepData;

    @SerializedName("rejection_reason")
    private String rejectionReason;

    @SerializedName("documents")
    private List<OwnerDocumentDto> documents;

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public int getCurrentStep() {
        return currentStep > 0 ? currentStep : 1;
    }

    public Map<String, Map<String, Object>> getStepData() {
        return stepData;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public List<OwnerDocumentDto> getDocuments() {
        return documents;
    }
}

package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class OwnerStatusDto {
    @SerializedName("status")
    private String status;

    @SerializedName("current_step")
    private int currentStep;

    @SerializedName("submitted_at")
    private String submittedAt;

    @SerializedName("verified_at")
    private String verifiedAt;

    @SerializedName("rejected_at")
    private String rejectedAt;

    @SerializedName("rejection_reason")
    private String rejectionReason;

    public String getStatus() {
        return status;
    }

    public int getCurrentStep() {
        return currentStep > 0 ? currentStep : 1;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isPendingReview() {
        return "under_review".equals(status) || "submitted".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public boolean isDraft() {
        return status == null || "draft".equals(status);
    }

    public boolean hasNeverApplied() {
        return status == null;
    }
}

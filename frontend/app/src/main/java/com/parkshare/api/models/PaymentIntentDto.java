package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class PaymentIntentDto {
    @SerializedName("payment_intent_id")
    private String paymentIntentId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("amount")
    private double amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("dev_mode")
    private boolean devMode;

    @SerializedName("publishable_key")
    private String publishableKey;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public String getPublishableKey() {
        return publishableKey;
    }
}

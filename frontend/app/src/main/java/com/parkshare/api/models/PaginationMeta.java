package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class PaginationMeta {
    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("total")
    private int total;

    public int getCurrentPage() {
        return currentPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public boolean hasMorePages() {
        return currentPage < lastPage;
    }
}

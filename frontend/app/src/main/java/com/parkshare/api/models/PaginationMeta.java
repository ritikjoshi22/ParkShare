package com.parkshare.api.models;

import com.google.gson.annotations.SerializedName;

public class PaginationMeta {
    @SerializedName("current_page")
    private long currentPage;

    @SerializedName("last_page")
    private long lastPage;

    @SerializedName("per_page")
    private long perPage;

    @SerializedName("total")
    private long total;

    public long getCurrentPage() {
        return currentPage;
    }

    public long getLastPage() {
        return lastPage;
    }

    public long getTotal() {
        return total;
    }

    public boolean hasMorePages() {
        return currentPage < lastPage;
    }
}

package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.models.OwnerStatsDto;
import com.parkshare.frontend.utils.RepositoryCallback;

public class OwnerRepository extends BaseRepository {

    public void getStats(RepositoryCallback<OwnerStatsDto> callback) {
        enqueue(ApiClient.getInstance().getApiService().ownerStats(), callback);
    }
}

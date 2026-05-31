package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.FavoriteDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getFavorites(int page, RepositoryCallback<List<FavoriteDto>> callback) {
        enqueue(api.favorites(page, 20), callback);
    }

    public void addFavorite(long parkingSpaceId, RepositoryCallback<FavoriteDto> callback) {
        Map<String, Long> body = new HashMap<>();
        body.put("parking_space_id", parkingSpaceId);
        enqueue(api.addFavorite(body), callback);
    }

    public void removeFavorite(long favoriteId, RepositoryCallback<Void> callback) {
        enqueueVoid(api.removeFavorite(favoriteId), callback);
    }
}

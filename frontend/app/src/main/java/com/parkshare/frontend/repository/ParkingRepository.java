package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class ParkingRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getNearby(double lat, double lng, int page, RepositoryCallback<List<ParkingSpaceDto>> callback) {
        enqueue(api.nearbyParking(lat, lng, 15, page, 15), callback);
    }

    public void getAll(int page, Double lat, Double lng, RepositoryCallback<List<ParkingSpaceDto>> callback) {
        enqueue(api.parkingSpaces(page, 15, lat, lng), callback);
    }

    public void getById(long id, RepositoryCallback<ParkingSpaceDto> callback) {
        enqueue(api.parkingSpace(id), callback);
    }

    public void create(java.util.Map<String, Object> body, RepositoryCallback<ParkingSpaceDto> callback) {
        enqueue(api.createParkingSpace(body), callback);
    }

    public void update(long id, java.util.Map<String, Object> body, RepositoryCallback<ParkingSpaceDto> callback) {
        enqueue(api.updateParkingSpace(id, body), callback);
    }

    public void delete(long id, RepositoryCallback<Void> callback) {
        enqueueVoid(api.deleteParkingSpace(id), callback);
    }
}

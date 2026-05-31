package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.SosRequestDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SosRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getRequests(int page, String status, RepositoryCallback<List<SosRequestDto>> callback) {
        enqueue(api.sosRequests(page, 15, status), callback);
    }

    public void createSos(double lat, double lng, String message, RepositoryCallback<SosRequestDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", lat);
        body.put("longitude", lng);
        if (message != null && !message.isEmpty()) {
            body.put("emergency_message", message);
        }
        enqueue(api.createSos(body), callback);
    }

    public void updateStatus(long id, String status, RepositoryCallback<SosRequestDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        enqueue(api.updateSosStatus(id, body), callback);
    }
}

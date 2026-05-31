package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class TechnicianRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getProfile(RepositoryCallback<TechnicianDto> callback) {
        enqueue(api.technicianProfile(), callback);
    }

    public void getTechnicians(int page, String status, RepositoryCallback<List<TechnicianDto>> callback) {
        enqueue(api.technicians(page, 15, status), callback);
    }
}

package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.api.models.TechnicianServiceDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnicianRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getProfile(RepositoryCallback<TechnicianDto> callback) {
        enqueue(api.technicianProfile(), callback);
    }

    public void getTechnicians(int page, String status, RepositoryCallback<List<TechnicianDto>> callback) {
        enqueue(api.technicians(page, 15, status), callback);
    }

    public void updateAvailability(long technicianId, String status, RepositoryCallback<TechnicianDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("availability_status", status);
        enqueue(api.updateTechnician(technicianId, body), callback);
    }

    public void addService(long technicianId, String serviceName, RepositoryCallback<TechnicianServiceDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("service_name", serviceName);
        enqueue(api.addTechnicianService(technicianId, body), callback);
    }

    public void deleteService(long serviceId, RepositoryCallback<Void> callback) {
        enqueueVoid(api.deleteTechnicianService(serviceId), callback);
    }
}

package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.OwnerDocumentDto;
import com.parkshare.api.models.OwnerProfileDto;
import com.parkshare.api.models.OwnerStatusDataDto;
import com.parkshare.api.models.OwnerStatsDto;
import com.parkshare.api.models.ParkingTechnicianDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class OwnerRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void fetchStatus(RepositoryCallback<OwnerStatusDataDto> callback) {
        enqueue(api.ownerStatus(), callback);
    }

    public void getStats(RepositoryCallback<OwnerStatsDto> callback) {
        enqueue(api.ownerStats(), callback);
    }

    public void fetchVerification(RepositoryCallback<OwnerProfileDto> callback) {
        enqueue(api.ownerVerification(), callback);
    }

    public void saveStep(int step, Map<String, Object> body, RepositoryCallback<OwnerProfileDto> callback) {
        enqueue(api.saveOwnerVerificationStep(step, body), callback);
    }

    public void submitVerification(RepositoryCallback<OwnerProfileDto> callback) {
        enqueue(api.submitOwnerVerification(), callback);
    }

    public void uploadDocument(String documentType, MultipartBody.Part file,
                               RepositoryCallback<OwnerDocumentDto> callback) {
        RequestBody typeBody = RequestBody.create(documentType, MediaType.parse("text/plain"));
        enqueue(api.uploadOwnerDocument(typeBody, file), callback);
    }

    public void deleteDocument(long documentId, RepositoryCallback<Void> callback) {
        enqueueVoid(api.deleteOwnerDocument(documentId), callback);
    }

    public void fetchTechnicians(long parkingSpaceId, RepositoryCallback<List<ParkingTechnicianDto>> callback) {
        enqueue(api.ownerParkingTechnicians(parkingSpaceId), callback);
    }

    public void addTechnician(long parkingSpaceId, Map<String, Object> body,
                              RepositoryCallback<ParkingTechnicianDto> callback) {
        enqueue(api.addParkingTechnician(parkingSpaceId, body), callback);
    }

    public void updateTechnician(long parkingSpaceId, long technicianId, Map<String, Object> body,
                                 RepositoryCallback<ParkingTechnicianDto> callback) {
        enqueue(api.updateParkingTechnician(parkingSpaceId, technicianId, body), callback);
    }

    public void deleteTechnician(long parkingSpaceId, long technicianId, RepositoryCallback<Void> callback) {
        enqueueVoid(api.deleteParkingTechnician(parkingSpaceId, technicianId), callback);
    }
}

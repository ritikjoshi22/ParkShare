package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.parkshare.api.models.ApiResponse;
import com.parkshare.frontend.utils.ApiErrorParser;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ParkingRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getNearby(double lat, double lng, int page, RepositoryCallback<List<ParkingSpaceDto>> callback) {
        enqueue(api.nearbyParking(lat, lng, 200, page, 15), callback);
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

    public void uploadImages(long parkingId, List<Uri> uris, RepositoryCallback<Void> callback) {
        if (uris == null || uris.isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        List<MultipartBody.Part> parts = new ArrayList<>();
        Context ctx = com.parkshare.frontend.ParkShareApplication.getAppContext();
        for (Uri uri : uris) {
            try {
                File file = uriToFile(ctx, uri);
                RequestBody body = RequestBody.create(file, MediaType.parse("image/*"));
                parts.add(MultipartBody.Part.createFormData("images[]", file.getName(), body));
            } catch (Exception e) {
                callback.onError(e.getMessage());
                return;
            }
        }
        api.uploadParkingImagesBatch(parkingId, parts).enqueue(new Callback<ApiResponse<List<com.parkshare.api.models.ParkingImageDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.parkshare.api.models.ParkingImageDto>>> call,
                                   Response<ApiResponse<List<com.parkshare.api.models.ParkingImageDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.parkshare.api.models.ParkingImageDto>>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Upload failed");
            }
        });
    }

    private File uriToFile(Context context, Uri uri) throws Exception {
        String name = "upload.jpg";
        try (Cursor c = context.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    name = c.getString(idx);
                }
            }
        }
        File out = new File(context.getCacheDir(), name);
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(out)) {
            if (in == null) {
                throw new Exception("Cannot read image");
            }
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        }
        return out;
    }
}

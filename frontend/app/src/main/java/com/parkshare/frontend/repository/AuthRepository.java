package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.AuthData;
import com.parkshare.api.models.UserDto;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository extends BaseRepository {

    private final ApiService api;
    private final SessionManager sessionManager;

    public AuthRepository(SessionManager sessionManager) {
        this.api = ApiClient.getInstance().getApiService();
        this.sessionManager = sessionManager;
    }

    public void login(String email, String password, RepositoryCallback<UserDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        api.login(body).enqueue(new retrofit2.Callback<com.parkshare.api.models.ApiResponse<AuthData>>() {
            @Override
            public void onResponse(retrofit2.Call<com.parkshare.api.models.ApiResponse<AuthData>> call,
                                   retrofit2.Response<com.parkshare.api.models.ApiResponse<AuthData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    AuthData data = response.body().getData();
                    sessionManager.saveSession(data.getToken(), data.getUser());
                    callback.onSuccess(data.getUser());
                } else {
                    callback.onError(com.parkshare.frontend.utils.ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.parkshare.api.models.ApiResponse<AuthData>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void register(Map<String, Object> body, RepositoryCallback<UserDto> callback) {
        api.register(body).enqueue(new retrofit2.Callback<com.parkshare.api.models.ApiResponse<AuthData>>() {
            @Override
            public void onResponse(retrofit2.Call<com.parkshare.api.models.ApiResponse<AuthData>> call,
                                   retrofit2.Response<com.parkshare.api.models.ApiResponse<AuthData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    AuthData data = response.body().getData();
                    sessionManager.saveSession(data.getToken(), data.getUser());
                    callback.onSuccess(data.getUser());
                } else {
                    callback.onError(com.parkshare.frontend.utils.ApiErrorParser.parse(response));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.parkshare.api.models.ApiResponse<AuthData>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void logout(RepositoryCallback<Void> callback) {
        enqueueVoid(api.logout(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                sessionManager.clearSession();
                ApiClient.reset();
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                sessionManager.clearSession();
                ApiClient.reset();
                callback.onSuccess(null);
            }
        });
    }

    public void fetchProfile(RepositoryCallback<UserDto> callback) {
        enqueue(api.profile(), callback);
    }
}

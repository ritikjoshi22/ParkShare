package com.parkshare.api;

import androidx.annotation.NonNull;

import com.parkshare.frontend.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public UnauthorizedInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == 401) {
            sessionManager.clearSession();
            sessionManager.notifyUnauthorized();
        }
        return response;
    }
}

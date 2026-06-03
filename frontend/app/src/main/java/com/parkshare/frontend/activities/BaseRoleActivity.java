package com.parkshare.frontend.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.utils.SessionManager;

public abstract class BaseRoleActivity extends AppCompatActivity {

    protected SessionManager sessionManager;
    private final Runnable unauthorizedListener = () -> {
        sessionManager.redirectToLogin(this);
        finish();
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.redirectToLogin(this);
            finish();
            return;
        }
        if (!isCorrectRole()) {
            com.parkshare.frontend.utils.RoleRouter.openDashboard(this);
            finish();
            return;
        }
        sessionManager.addUnauthorizedListener(unauthorizedListener);
    }

    protected abstract boolean isCorrectRole();

    @Override
    protected void onDestroy() {
        if (sessionManager != null) {
            sessionManager.removeUnauthorizedListener(unauthorizedListener);
        }
        super.onDestroy();
    }
}

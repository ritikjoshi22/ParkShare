package com.parkshare.frontend.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.utils.RoleRouter;
import com.parkshare.frontend.utils.SessionManager;

/**
 * Legacy entry point — redirects to the role-specific dashboard.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.redirectToLogin(this);
            finish();
            return;
        }
        RoleRouter.openDashboard(this);
        finish();
    }
}

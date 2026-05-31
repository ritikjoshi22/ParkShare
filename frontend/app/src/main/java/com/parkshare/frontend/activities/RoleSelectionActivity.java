package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.databinding.ActivityRoleSelectionBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.utils.RoleRouter;
import com.parkshare.frontend.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class RoleSelectionActivity extends AppCompatActivity {

    private ActivityRoleSelectionBinding binding;
    private AuthRepository authRepository;
    private String fullName;
    private String email;
    private String phone;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fullName = getIntent().getStringExtra(SignupActivity.EXTRA_FULL_NAME);
        email = getIntent().getStringExtra(SignupActivity.EXTRA_EMAIL);
        phone = getIntent().getStringExtra(SignupActivity.EXTRA_PHONE);
        password = getIntent().getStringExtra(SignupActivity.EXTRA_PASSWORD);

        if (fullName == null || email == null || phone == null || password == null) {
            Toast.makeText(this, "Complete signup form first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authRepository = new AuthRepository(SessionManager.getInstance(this));

        binding.cardDriver.setOnClickListener(v -> registerAs("driver"));
        binding.cardOwner.setOnClickListener(v -> registerAs("owner"));
        binding.cardTechnician.setOnClickListener(v -> registerAs("technician"));
    }

    private void registerAs(String role) {
        setLoading(true);
        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", password);
        body.put("password_confirmation", password);
        body.put("role", role);

        authRepository.register(body, new com.parkshare.frontend.utils.RepositoryCallback<com.parkshare.api.models.UserDto>() {
            @Override
            public void onSuccess(com.parkshare.api.models.UserDto data) {
                setLoading(false);
                RoleRouter.openDashboard(RoleSelectionActivity.this);
                finishAffinity();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RoleSelectionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.cardDriver.setEnabled(!loading);
        binding.cardOwner.setEnabled(!loading);
        binding.cardTechnician.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}

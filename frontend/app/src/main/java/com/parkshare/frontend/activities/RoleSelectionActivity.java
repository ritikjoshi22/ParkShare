package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.databinding.ActivityRoleSelectionBinding;

public class RoleSelectionActivity extends AppCompatActivity {

    private ActivityRoleSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardDriver.setOnClickListener(v -> navigateToMain("driver"));
        binding.cardOwner.setOnClickListener(v -> navigateToMain("owner"));
        binding.cardTechnician.setOnClickListener(v -> navigateToMain("technician"));
    }

    private void navigateToMain(String role) {
        // Save role to preferences (Placeholder)
        Intent intent = new Intent(RoleSelectionActivity.this, MainActivity.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
        finish();
    }
}
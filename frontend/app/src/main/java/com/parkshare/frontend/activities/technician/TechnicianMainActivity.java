package com.parkshare.frontend.activities.technician;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BaseRoleActivity;
import com.parkshare.frontend.databinding.ActivityTechnicianMainBinding;

import java.util.HashSet;
import java.util.Set;

public class TechnicianMainActivity extends BaseRoleActivity {

    private ActivityTechnicianMainBinding binding;

    @Override
    protected boolean isCorrectRole() {
        return sessionManager.isTechnician();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }
        binding = ActivityTechnicianMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.technician_app_title);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        NavController navController = navHostFragment.getNavController();

        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.tech_dashboard);
        topLevel.add(R.id.tech_requests);
        topLevel.add(R.id.tech_services);
        topLevel.add(R.id.tech_earnings);
        topLevel.add(R.id.tech_profile);

        AppBarConfiguration config = new AppBarConfiguration.Builder(topLevel).build();
        NavigationUI.setupActionBarWithNavController(this, navController, config);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}

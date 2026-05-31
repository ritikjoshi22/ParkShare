package com.parkshare.frontend.activities.driver;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BaseRoleActivity;
import com.parkshare.frontend.databinding.ActivityDriverMainBinding;

import java.util.HashSet;
import java.util.Set;

public class DriverMainActivity extends BaseRoleActivity {

    private ActivityDriverMainBinding binding;

    @Override
    protected boolean isCorrectRole() {
        return sessionManager.isDriver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }
        binding = ActivityDriverMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.driver_app_title);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        NavController navController = navHostFragment.getNavController();

        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.driver_home);
        topLevel.add(R.id.driver_bookings);
        topLevel.add(R.id.driver_sos);
        topLevel.add(R.id.driver_notifications);
        topLevel.add(R.id.driver_profile);

        AppBarConfiguration config = new AppBarConfiguration.Builder(topLevel).build();
        NavigationUI.setupActionBarWithNavController(this, navController, config);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}

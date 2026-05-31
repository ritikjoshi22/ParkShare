package com.parkshare.frontend.activities.owner;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BaseRoleActivity;
import com.parkshare.frontend.databinding.ActivityOwnerMainBinding;

import java.util.HashSet;
import java.util.Set;

public class OwnerMainActivity extends BaseRoleActivity {

    private ActivityOwnerMainBinding binding;

    @Override
    protected boolean isCorrectRole() {
        return sessionManager.isOwner();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }
        binding = ActivityOwnerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.owner_app_title);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        NavController navController = navHostFragment.getNavController();

        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.owner_dashboard);
        topLevel.add(R.id.owner_parking);
        topLevel.add(R.id.owner_bookings);
        topLevel.add(R.id.owner_analytics);
        topLevel.add(R.id.owner_profile);

        AppBarConfiguration config = new AppBarConfiguration.Builder(topLevel).build();
        NavigationUI.setupActionBarWithNavController(this, navController, config);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}

package com.parkshare.frontend.activities.driver;

import android.os.Bundle;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BaseRoleActivity;
import com.parkshare.frontend.databinding.ActivityDriverMainBinding;

import com.parkshare.api.models.NotificationDto;
import com.parkshare.frontend.repository.NotificationRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DriverMainActivity extends BaseRoleActivity {

    private ActivityDriverMainBinding binding;
    private final NotificationRepository notificationRepository = new NotificationRepository();

    @Override
    protected boolean isCorrectRole() {
        return sessionManager.isDriver();
    }

    public static final String EXTRA_TARGET_TAB = "target_tab";

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
        topLevel.add(R.id.driver_map);
        topLevel.add(R.id.driver_bookings);
        topLevel.add(R.id.driver_notifications);
        topLevel.add(R.id.driver_sos);
        topLevel.add(R.id.driver_profile);

        AppBarConfiguration config = new AppBarConfiguration.Builder(topLevel).build();
        NavigationUI.setupActionBarWithNavController(this, navController, config);
        NavigationUI.setupWithNavController(binding.navView, navController);

        handleTargetNavigation(navController);
        updateNotificationBadge();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            handleTargetNavigation(navHostFragment.getNavController());
        }
    }

    private void handleTargetNavigation(NavController navController) {
        int targetTab = getIntent().getIntExtra(EXTRA_TARGET_TAB, -1);
        if (targetTab != -1) {
            navController.navigate(targetTab);
        }
    }

    private void updateNotificationBadge() {
        notificationRepository.getNotifications(1, true, new RepositoryCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> data) {
                if (data != null && !data.isEmpty()) {
                    // In a real app, you'd update a badge on the menu item.
                    // For now, we just log or could use a custom view.
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.action_notifications);
        View actionView = item.getActionView();
        if (actionView != null) {
            actionView.setOnClickListener(v -> {
                startActivity(new Intent(this, com.parkshare.frontend.activities.NotificationsActivity.class));
            });
            updateBadgeUI(actionView);
        }
        return true;
    }

    private void updateBadgeUI(View actionView) {
        android.widget.TextView tvBadge = actionView.findViewById(R.id.tvBadge);
        notificationRepository.getNotifications(1, true, new RepositoryCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> data) {
                if (data != null && !data.isEmpty()) {
                    tvBadge.setText(String.valueOf(data.size()));
                    tvBadge.setVisibility(View.VISIBLE);
                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                tvBadge.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, com.parkshare.frontend.activities.NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

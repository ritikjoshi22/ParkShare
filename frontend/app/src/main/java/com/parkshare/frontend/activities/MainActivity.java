package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityMainBinding;
import com.parkshare.frontend.repository.NotificationRepository;
import com.parkshare.frontend.utils.SessionManager;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private int unreadCount = 0;
    private final Runnable unauthorizedListener = () -> {
        sessionManager.redirectToLogin(this);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.redirectToLogin(this);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        sessionManager.addUnauthorizedListener(unauthorizedListener);
        configureNavigationForRole();
        loadUnreadNotifications();
    }

    private void configureNavigationForRole() {
        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.navigation_home);
        topLevel.add(R.id.navigation_profile);

        if (sessionManager.isDriver() || sessionManager.isOwner()) {
            topLevel.add(R.id.navigation_bookings);
            binding.navView.getMenu().findItem(R.id.navigation_bookings).setVisible(true);
        } else {
            binding.navView.getMenu().findItem(R.id.navigation_bookings).setVisible(false);
        }

        if (sessionManager.isDriver() || sessionManager.isTechnician()) {
            topLevel.add(R.id.navigation_sos);
            binding.navView.getMenu().findItem(R.id.navigation_sos).setVisible(true);
        } else {
            binding.navView.getMenu().findItem(R.id.navigation_sos).setVisible(false);
        }

        if (sessionManager.isOwner()) {
            binding.toolbar.setTitle(R.string.my_parking_spaces);
        } else if (sessionManager.isTechnician()) {
            binding.toolbar.setTitle(R.string.technician_dashboard);
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevel).build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (sessionManager.isTechnician()) {
            binding.navView.setSelectedItemId(R.id.navigation_sos);
        }
    }

    private void loadUnreadNotifications() {
        new NotificationRepository().getNotifications(1, true,
                new com.parkshare.frontend.utils.RepositoryCallback<java.util.List<com.parkshare.api.models.NotificationDto>>() {
                    @Override
                    public void onSuccess(java.util.List<com.parkshare.api.models.NotificationDto> data) {
                        unreadCount = data != null ? data.size() : 0;
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onError(String message) {
                        unreadCount = 0;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.action_notifications);
        if (unreadCount > 0) {
            item.setTitle(getString(R.string.notifications) + " (" + unreadCount + ")");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadNotifications();
    }

    @Override
    protected void onDestroy() {
        sessionManager.removeUnauthorizedListener(unauthorizedListener);
        super.onDestroy();
    }
}

package com.parkshare.frontend.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.NotificationDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.NotificationAdapter;
import com.parkshare.frontend.databinding.ActivityNotificationsBinding;
import com.parkshare.frontend.repository.NotificationRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private ActivityNotificationsBinding binding;
    private NotificationRepository notificationRepository;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        notificationRepository = new NotificationRepository();
        adapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        binding.btnRetry.setOnClickListener(v -> loadNotifications());
        loadNotifications();
    }

    private void loadNotifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);

        notificationRepository.getNotifications(1, false, new RepositoryCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setItems(data);
                boolean empty = data == null || data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvError.setText(message);
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationDto notification) {
        if (!notification.isRead()) {
            notificationRepository.markRead(notification.getId(), new RepositoryCallback<NotificationDto>() {
                @Override
                public void onSuccess(NotificationDto data) {
                    loadNotifications();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(NotificationsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notifications_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            notificationRepository.markAllRead(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadNotifications();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(NotificationsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

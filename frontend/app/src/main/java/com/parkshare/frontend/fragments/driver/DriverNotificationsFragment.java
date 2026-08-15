package com.parkshare.frontend.fragments.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.NotificationDto;
import com.parkshare.frontend.adapters.NotificationAdapter;
import com.parkshare.frontend.databinding.FragmentDriverNotificationsBinding;
import com.parkshare.frontend.repository.NotificationRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;

public class DriverNotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private FragmentDriverNotificationsBinding binding;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()));
        binding = FragmentDriverNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMap();
        adapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);
        binding.btnRetry.setOnClickListener(v -> load());
        binding.btnMarkAllRead.setOnClickListener(v ->
                new NotificationRepository().markAllRead(new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        load();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }));
        load();
    }

    private void setupMap() {
        MapView map = binding.mapNearbyTech;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(false);
        map.getController().setZoom(14.0);
        map.getController().setCenter(new GeoPoint(27.7172, 85.3240));
    }

    private void load() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new NotificationRepository().getNotifications(1, false, new RepositoryCallback<List<NotificationDto>>() {
            @Override
            public void onSuccess(List<NotificationDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setItems(data);
                binding.layoutEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(message);
            }
        });
    }

    @Override
    public void onNotificationClick(NotificationDto notification) {
        if (!notification.isRead()) {
            new NotificationRepository().markRead(notification.getId(), new RepositoryCallback<NotificationDto>() {
                @Override
                public void onSuccess(NotificationDto data) {
                    load();
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.parkshare.frontend.fragments.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parkshare.api.models.OwnerStatsDto;
import com.parkshare.frontend.databinding.FragmentOwnerDashboardBinding;
import com.parkshare.frontend.repository.OwnerRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.Locale;

public class OwnerDashboardFragment extends Fragment {

    private FragmentOwnerDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.swipeRefresh.setOnRefreshListener(this::loadStats);
        loadStats();
    }

    private void loadStats() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new OwnerRepository().getStats(new RepositoryCallback<OwnerStatsDto>() {
            @Override
            public void onSuccess(OwnerStatsDto data) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (data == null) {
                    return;
                }
                binding.tvTotalSpaces.setText(String.valueOf(data.getTotalParkingSpaces()));
                binding.tvActiveBookings.setText(String.valueOf(data.getActiveBookings()));
                binding.tvMonthlyRevenue.setText(String.format(Locale.getDefault(), "NPR %.0f", data.getMonthlyRevenue()));
                binding.tvAvailableSlots.setText(String.valueOf(data.getAvailableSlots()));
                binding.tvOccupancy.setText(String.format(Locale.getDefault(), "%.1f%%", data.getOccupancyRate()));
                binding.tvTopParking.setText(data.getTopParkingName() != null
                        ? data.getTopParkingName() : "—");
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.tvTopParking.setText(message);
            }
        });
    }
}

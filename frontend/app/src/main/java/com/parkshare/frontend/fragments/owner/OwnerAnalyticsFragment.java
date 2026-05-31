package com.parkshare.frontend.fragments.owner;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.parkshare.api.models.OwnerStatsDto;
import com.parkshare.frontend.databinding.FragmentOwnerAnalyticsBinding;
import com.parkshare.frontend.repository.OwnerRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.Locale;

public class OwnerAnalyticsFragment extends Fragment {

    private FragmentOwnerAnalyticsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new OwnerRepository().getStats(new RepositoryCallback<OwnerStatsDto>() {
            @Override
            public void onSuccess(OwnerStatsDto data) {
                if (data == null) {
                    return;
                }
                binding.tvRevenue.setText(String.format(Locale.getDefault(), "NPR %.0f", data.getMonthlyRevenue()));
                binding.tvOccupancy.setText(String.format(Locale.getDefault(), "%.1f%% occupied", data.getOccupancyRate()));
                binding.tvTopParking.setText(data.getTopParkingName() != null ? data.getTopParkingName() : "—");

                ArrayList<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0f, data.getActiveBookings()));
                entries.add(new BarEntry(1f, data.getTotalParkingSpaces()));
                entries.add(new BarEntry(2f, (float) data.getOccupancyRate()));

                BarDataSet set = new BarDataSet(entries, "Metrics");
                set.setColor(Color.parseColor("#2196F3"));
                BarData barData = new BarData(set);
                binding.chart.setData(barData);
                binding.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                binding.chart.getDescription().setEnabled(false);
                binding.chart.invalidate();
            }

            @Override
            public void onError(String message) {
                binding.tvRevenue.setText(message);
            }
        });
    }
}

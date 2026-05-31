package com.parkshare.frontend.fragments.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parkshare.api.models.SosRequestDto;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.frontend.databinding.FragmentTechnicianEarningsBinding;
import com.parkshare.frontend.repository.SosRepository;
import com.parkshare.frontend.repository.TechnicianRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;
import java.util.Locale;

public class TechnicianEarningsFragment extends Fragment {

    private FragmentTechnicianEarningsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechnicianEarningsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new TechnicianRepository().getProfile(new RepositoryCallback<TechnicianDto>() {
            @Override
            public void onSuccess(TechnicianDto data) {
                if (data == null) {
                    return;
                }
                double rate = data.getHourlyRate() != null ? data.getHourlyRate() : 0;
                new SosRepository().getRequests(1, "resolved", new RepositoryCallback<List<SosRequestDto>>() {
                    @Override
                    public void onSuccess(List<SosRequestDto> resolved) {
                        int jobs = resolved != null ? resolved.size() : 0;
                        binding.tvCompletedJobs.setText(String.valueOf(jobs));
                        binding.tvMonthlyEarnings.setText(String.format(Locale.getDefault(), "NPR %.0f", jobs * rate * 2));
                        binding.tvDailyEarnings.setText(String.format(Locale.getDefault(), "NPR %.0f", rate * 2));
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }

            @Override
            public void onError(String message) {
                binding.tvMonthlyEarnings.setText(message);
            }
        });
    }
}

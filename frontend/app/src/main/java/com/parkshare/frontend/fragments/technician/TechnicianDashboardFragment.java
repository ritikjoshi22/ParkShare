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
import com.parkshare.frontend.databinding.FragmentTechnicianDashboardBinding;
import com.parkshare.frontend.repository.SosRepository;
import com.parkshare.frontend.repository.TechnicianRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;
import java.util.Locale;

public class TechnicianDashboardFragment extends Fragment {

    private FragmentTechnicianDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechnicianDashboardBinding.inflate(inflater, container, false);
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
                binding.tvAvailability.setText(data.getAvailabilityStatus());
                binding.tvSpecialization.setText(data.getSpecialization());
                if (data.getReviewsAvgRating() != null) {
                    binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", data.getReviewsAvgRating()));
                }
            }

            @Override
            public void onError(String message) {
                binding.tvAvailability.setText(message);
            }
        });

        new SosRepository().getRequests(1, "active", new RepositoryCallback<List<SosRequestDto>>() {
            @Override
            public void onSuccess(List<SosRequestDto> data) {
                binding.tvActiveRequests.setText(String.valueOf(data != null ? data.size() : 0));
            }

            @Override
            public void onError(String message) {
                binding.tvActiveRequests.setText("0");
            }
        });

        new SosRepository().getRequests(1, "resolved", new RepositoryCallback<List<SosRequestDto>>() {
            @Override
            public void onSuccess(List<SosRequestDto> data) {
                binding.tvCompletedJobs.setText(String.valueOf(data != null ? data.size() : 0));
            }

            @Override
            public void onError(String message) {
            }
        });
    }
}

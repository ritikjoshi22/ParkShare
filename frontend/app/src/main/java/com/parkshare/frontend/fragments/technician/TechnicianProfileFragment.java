package com.parkshare.frontend.fragments.technician;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parkshare.api.models.TechnicianDto;
import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.databinding.FragmentTechnicianProfileBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.repository.TechnicianRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

public class TechnicianProfileFragment extends Fragment {

    private FragmentTechnicianProfileBinding binding;
    private long technicianId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechnicianProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = SessionManager.getInstance(requireContext());
        binding.tvName.setText(session.getFullName());
        binding.tvEmail.setText(session.getEmail());

        new TechnicianRepository().getProfile(new RepositoryCallback<TechnicianDto>() {
            @Override
            public void onSuccess(TechnicianDto data) {
                if (data == null) {
                    return;
                }
                technicianId = data.getId();
                binding.tvSpecialization.setText(data.getSpecialization());
                binding.tvExperience.setText(getString(com.parkshare.frontend.R.string.experience_years, data.getExperienceYears()));
                binding.tvRadius.setText(getString(com.parkshare.frontend.R.string.service_radius, data.getServiceRadiusKm()));
                binding.switchAvailable.setChecked("available".equals(data.getAvailabilityStatus()));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        binding.switchAvailable.setOnCheckedChangeListener((btn, checked) -> {
            if (technicianId > 0) {
                String status = checked ? "available" : "offline";
                new TechnicianRepository().updateAvailability(technicianId, status, new RepositoryCallback<TechnicianDto>() {
                    @Override
                    public void onSuccess(TechnicianDto data) {
                        Toast.makeText(requireContext(), "Availability updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        binding.btnLogout.setOnClickListener(v ->
                new AuthRepository(session).logout(new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        startActivity(new Intent(requireContext(), LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        requireActivity().finish();
                    }

                    @Override
                    public void onError(String message) {
                    }
                }));
    }
}

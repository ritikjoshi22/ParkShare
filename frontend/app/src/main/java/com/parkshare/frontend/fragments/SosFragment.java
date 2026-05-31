package com.parkshare.frontend.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.api.models.SosRequestDto;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.FragmentSosBinding;
import com.parkshare.frontend.repository.SosRepository;
import com.parkshare.frontend.repository.TechnicianRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class SosFragment extends Fragment {

    private FragmentSosBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private SosRepository sosRepository;
    private double latitude = 27.7172;
    private double longitude = 85.3240;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sosRepository = new SosRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SessionManager session = SessionManager.getInstance(requireContext());

        binding.btnSos.setOnClickListener(v -> showEmergencyConfirmation());

        if (session.isTechnician()) {
            binding.btnSos.setVisibility(View.GONE);
            binding.tvSosDescription.setText("Active SOS requests assigned to you appear below.");
            loadTechnicianSos();
        } else {
            detectLocation();
            loadActiveSosForDriver();
        }
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    binding.tvCurrentLocation.setText(String.format(Locale.getDefault(),
                            "%.5f, %.5f", latitude, longitude));
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2001);
        }
    }

    private void loadActiveSosForDriver() {
        sosRepository.getRequests(1, "active", new RepositoryCallback<List<SosRequestDto>>() {
            @Override
            public void onSuccess(List<SosRequestDto> data) {
                if (data != null && !data.isEmpty()) {
                    showAssignedTechnician(data.get(0));
                }
            }

            @Override
            public void onError(String message) {
                // no active SOS is fine
            }
        });
    }

    private void loadTechnicianSos() {
        sosRepository.getRequests(1, "active", new RepositoryCallback<List<SosRequestDto>>() {
            @Override
            public void onSuccess(List<SosRequestDto> data) {
                if (data != null && !data.isEmpty()) {
                    SosRequestDto sos = data.get(0);
                    binding.tvTechnicianStatus.setVisibility(View.VISIBLE);
                    binding.tvTechnicianStatus.setText("Active SOS #" + sos.getId()
                            + " at " + sos.getLatitude() + ", " + sos.getLongitude());
                }
            }

            @Override
            public void onError(String message) {
                binding.tvTechnicianStatus.setVisibility(View.VISIBLE);
                binding.tvTechnicianStatus.setText(message);
            }
        });

        new TechnicianRepository().getProfile(new RepositoryCallback<TechnicianDto>() {
            @Override
            public void onSuccess(TechnicianDto data) {
                if (data != null && data.getUser() != null) {
                    binding.tvCurrentLocation.setText("Status: " + data.getAvailabilityStatus()
                            + " • " + data.getSpecialization());
                }
            }

            @Override
            public void onError(String message) {
                // optional
            }
        });
    }

    private void showAssignedTechnician(SosRequestDto sos) {
        binding.tvTechnicianStatus.setVisibility(View.VISIBLE);
        TechnicianDto tech = sos.getTechnician();
        if (tech != null && tech.getUser() != null) {
            binding.tvTechnicianStatus.setText(getString(R.string.assigned_technician) + ": "
                    + tech.getUser().getFullName() + " (" + tech.getSpecialization() + ")");
        } else {
            binding.tvTechnicianStatus.setText(R.string.no_technician_yet);
        }
    }

    private void showEmergencyConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_emergency)
                .setMessage(R.string.emergency_msg)
                .setPositiveButton(R.string.send_sos, (dialog, which) -> sendSos())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void sendSos() {
        detectLocation();
        binding.btnSos.setEnabled(false);
        sosRepository.createSos(latitude, longitude, "Emergency assistance needed", new RepositoryCallback<SosRequestDto>() {
            @Override
            public void onSuccess(SosRequestDto data) {
                binding.btnSos.setEnabled(true);
                Toast.makeText(getContext(), R.string.sos_sent, Toast.LENGTH_LONG).show();
                if (data != null) {
                    showAssignedTechnician(data);
                }
            }

            @Override
            public void onError(String message) {
                binding.btnSos.setEnabled(true);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

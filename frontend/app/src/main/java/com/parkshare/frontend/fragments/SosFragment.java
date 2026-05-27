package com.parkshare.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.parkshare.frontend.databinding.FragmentSosBinding;

public class SosFragment extends Fragment {

    private FragmentSosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSos.setOnClickListener(v -> showEmergencyConfirmation());
    }

    private void showEmergencyConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Emergency")
                .setMessage("Are you sure you want to send an emergency SOS request? Local technicians and services will be notified.")
                .setPositiveButton("SEND SOS", (dialog, which) -> {
                    Toast.makeText(getContext(), "Emergency Signal Sent!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
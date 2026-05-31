package com.parkshare.frontend.fragments.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.api.models.TechnicianServiceDto;
import com.parkshare.frontend.adapters.TechnicianServiceAdapter;
import com.parkshare.frontend.databinding.FragmentTechnicianServicesBinding;
import com.parkshare.frontend.repository.TechnicianRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class TechnicianServicesFragment extends Fragment implements TechnicianServiceAdapter.Listener {

    private FragmentTechnicianServicesBinding binding;
    private TechnicianServiceAdapter adapter;
    private long technicianId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTechnicianServicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new TechnicianServiceAdapter(this);
        binding.rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvServices.setAdapter(adapter);
        binding.fabAdd.setOnClickListener(v -> showAddDialog());
        loadProfileAndServices();
    }

    private void loadProfileAndServices() {
        new TechnicianRepository().getProfile(new RepositoryCallback<TechnicianDto>() {
            @Override
            public void onSuccess(TechnicianDto data) {
                if (data != null) {
                    technicianId = data.getId();
                    adapter.setItems(data.getServices());
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddDialog() {
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint("Service name");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Service")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!name.isEmpty() && technicianId > 0) {
                        new TechnicianRepository().addService(technicianId, name, new RepositoryCallback<TechnicianServiceDto>() {
                            @Override
                            public void onSuccess(TechnicianServiceDto data) {
                                loadProfileAndServices();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(TechnicianServiceDto service) {
        new TechnicianRepository().deleteService(service.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadProfileAndServices();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

package com.parkshare.frontend.fragments.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.activities.owner.ManageParkingActivity;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.FragmentOwnerParkingBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class OwnerParkingFragment extends Fragment implements ParkingAdapter.OnParkingActionListener {

    private FragmentOwnerParkingBinding binding;
    private ParkingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerParkingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvParking.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvParking.setAdapter(adapter);
        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ManageParkingActivity.class)));
        binding.swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new ParkingRepository().getAll(1, null, null, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                List<Parking> list = new ArrayList<>();
                if (data != null) {
                    for (ParkingSpaceDto dto : data) {
                        list.add(ParkingMapper.fromDto(dto));
                    }
                }
                adapter.updateList(list);
                binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onParkingClick(Parking parking) {
        Intent intent = new Intent(requireContext(), ManageParkingActivity.class);
        intent.putExtra(ManageParkingActivity.EXTRA_PARKING_ID, Long.parseLong(parking.getId()));
        startActivity(intent);
    }

    @Override
    public void onViewOnMapClick(Parking parking) {
        onParkingClick(parking);
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

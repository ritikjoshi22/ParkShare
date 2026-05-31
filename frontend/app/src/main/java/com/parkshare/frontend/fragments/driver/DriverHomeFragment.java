package com.parkshare.frontend.fragments.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.FavoritesActivity;
import com.parkshare.frontend.activities.ParkingDetailsActivity;
import com.parkshare.frontend.activities.ParkingMapActivity;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.FragmentDriverHomeBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverHomeFragment extends Fragment implements ParkingAdapter.OnParkingActionListener {

    private FragmentDriverHomeBinding binding;
    private ParkingAdapter recommendedAdapter;
    private ParkingAdapter nearbyAdapter;
    private final List<Parking> allParkingList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private ParkingRepository parkingRepository;
    private double userLat = 27.7172;
    private double userLng = 85.3240;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDriverHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parkingRepository = new ParkingRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        recommendedAdapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvRecommended.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecommended.setAdapter(recommendedAdapter);

        nearbyAdapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNearby.setAdapter(nearbyAdapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadParkingData);
        binding.btnRetry.setOnClickListener(v -> loadParkingData());
        binding.btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ParkingMapActivity.class);
            intent.putExtra(ParkingMapActivity.EXTRA_LAT, userLat);
            intent.putExtra(ParkingMapActivity.EXTRA_LNG, userLng);
            startActivity(intent);
        });
        binding.btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(getContext(), FavoritesActivity.class)));

        checkLocationPermission();
    }

    private void loadParkingData() {
        binding.layoutError.setVisibility(View.GONE);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                allParkingList.clear();
                if (data != null) {
                    for (ParkingSpaceDto dto : data) {
                        Parking parking = ParkingMapper.fromDto(dto);
                        if (dto.getDistanceKm() != null) {
                            parking.setDistance(String.format("%.1f km away", dto.getDistanceKm()));
                        }
                        allParkingList.add(parking);
                    }
                }
                List<Parking> recommended = allParkingList.size() > 3
                        ? new ArrayList<>(allParkingList.subList(0, 3))
                        : new ArrayList<>(allParkingList);
                recommendedAdapter.updateList(recommended);
                nearbyAdapter.updateList(new ArrayList<>(allParkingList));
                if (allParkingList.isEmpty()) {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvError.setText(R.string.no_parking_found);
                }
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvError.setText(message);
            }
        });
    }

    private void filter(String text) {
        List<Parking> filtered = new ArrayList<>();
        String q = text != null ? text.toLowerCase() : "";
        for (Parking p : allParkingList) {
            if (p.getName().toLowerCase().contains(q) || p.getAddress().toLowerCase().contains(q)) {
                filtered.add(p);
            }
        }
        nearbyAdapter.updateList(filtered);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            getCurrentLocation();
        }
        loadParkingData();
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    for (Parking parking : allParkingList) {
                        float[] results = new float[1];
                        Location.distanceBetween(userLat, userLng, parking.getLatitude(), parking.getLongitude(), results);
                        float m = results[0];
                        parking.setDistance(m >= 1000 ? String.format("%.1f km away", m / 1000f) : (int) m + " m away");
                    }
                    recommendedAdapter.notifyDataSetChanged();
                    nearbyAdapter.notifyDataSetChanged();
                }
            });
        } catch (SecurityException ignored) {
        }
    }

    @Override
    public void onParkingClick(Parking parking) {
        Intent intent = new Intent(getContext(), ParkingDetailsActivity.class);
        intent.putExtra(ParkingDetailsActivity.EXTRA_PARKING_ID, Long.parseLong(parking.getId()));
        startActivity(intent);
    }

    @Override
    public void onViewOnMapClick(Parking parking) {
        Intent intent = new Intent(getContext(), ParkingMapActivity.class);
        intent.putExtra(ParkingMapActivity.EXTRA_LAT, userLat);
        intent.putExtra(ParkingMapActivity.EXTRA_LNG, userLng);
        intent.putExtra(ParkingMapActivity.EXTRA_SELECTED_PARKING, parking);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

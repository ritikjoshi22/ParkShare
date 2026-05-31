package com.parkshare.frontend.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.activities.ParkingDetailsActivity;
import com.parkshare.frontend.activities.ParkingMapActivity;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.FragmentHomeBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ParkingAdapter.OnParkingActionListener {

    private FragmentHomeBinding binding;
    private ParkingAdapter recommendedAdapter;
    private ParkingAdapter nearbyAdapter;
    private final List<Parking> allParkingList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private ParkingRepository parkingRepository;
    private SessionManager sessionManager;
    private double userLat = 27.7172;
    private double userLng = 85.3240;
    private int currentPage = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        parkingRepository = new ParkingRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupRecyclerViews();
        setupSearchView();
        binding.swipeRefresh.setOnRefreshListener(this::loadParkingData);
        binding.btnRetry.setOnClickListener(v -> loadParkingData());

        checkLocationPermission();
    }

    private void setupRecyclerViews() {
        recommendedAdapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvRecommended.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecommended.setAdapter(recommendedAdapter);

        nearbyAdapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNearby.setAdapter(nearbyAdapter);
    }

    private void setupSearchView() {
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
    }

    private void loadParkingData() {
        showError(false);
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        RepositoryCallback<List<ParkingSpaceDto>> callback = new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                allParkingList.clear();
                if (data != null) {
                    for (ParkingSpaceDto dto : data) {
                        Parking parking = ParkingMapper.fromDto(dto);
                        if (parking.getDistance() == null && dto.getDistanceKm() != null) {
                            parking.setDistance(String.format("%.1f km away", dto.getDistanceKm()));
                        }
                        allParkingList.add(parking);
                    }
                }
                updateLists();
                if (allParkingList.isEmpty()) {
                    showError(true, "No parking spaces found nearby");
                }
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showError(true, message);
            }
        };

        if (sessionManager.isOwner()) {
            parkingRepository.getAll(currentPage, null, null, callback);
        } else {
            parkingRepository.getNearby(userLat, userLng, currentPage, callback);
        }
    }

    private void updateLists() {
        List<Parking> recommended = allParkingList.size() > 3
                ? new ArrayList<>(allParkingList.subList(0, 3))
                : new ArrayList<>(allParkingList);
        recommendedAdapter.updateList(recommended);
        nearbyAdapter.updateList(new ArrayList<>(allParkingList));
    }

    private void filter(String text) {
        List<Parking> filteredList = new ArrayList<>();
        String query = text != null ? text.toLowerCase() : "";
        for (Parking item : allParkingList) {
            if (item.getName().toLowerCase().contains(query)
                    || item.getAddress().toLowerCase().contains(query)) {
                filteredList.add(item);
            }
        }
        nearbyAdapter.updateList(filteredList);
    }

    private void showError(boolean show) {
        showError(show, null);
    }

    private void showError(boolean show, String message) {
        binding.layoutError.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null) {
            binding.tvError.setText(message);
        }
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
                    updateDistances(location);
                }
            });
        } catch (SecurityException e) {
            // default Kathmandu coordinates already set
        }
    }

    private void updateDistances(Location userLocation) {
        for (Parking parking : allParkingList) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    parking.getLatitude(), parking.getLongitude(), results);
            float distanceInMeters = results[0];
            if (distanceInMeters >= 1000) {
                parking.setDistance(String.format("%.1f km away", distanceInMeters / 1000.0));
            } else {
                parking.setDistance((int) distanceInMeters + " m away");
            }
        }
        recommendedAdapter.notifyDataSetChanged();
        nearbyAdapter.notifyDataSetChanged();
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
        intent.putExtra("selected_parking", parking);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
            loadParkingData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

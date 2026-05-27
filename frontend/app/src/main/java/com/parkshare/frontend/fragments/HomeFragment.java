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
import com.parkshare.frontend.activities.ParkingDetailsActivity;
import com.parkshare.frontend.activities.ParkingMapActivity;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.FragmentHomeBinding;
import com.parkshare.frontend.models.Parking;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ParkingAdapter.OnParkingActionListener {

    private FragmentHomeBinding binding;
    private ParkingAdapter recommendedAdapter;
    private ParkingAdapter nearbyAdapter;
    private List<Parking> allParkingList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        loadDummyData();
        setupRecyclerViews();
        setupSearchView();
        checkLocationPermission();
    }

    private void loadDummyData() {
        allParkingList.clear();
        allParkingList.add(new Parking("1", "Kathmandu Mall Parking", "Kanti Path, Kathmandu", 27.7029, 85.3120, 50.0, 100, 12, 4.8, "", "Multi-story secure parking facility.", "24/7", true));
        allParkingList.add(new Parking("2", "Civil Mall Parking", "Sundhara, Kathmandu", 27.7006, 85.3121, 60.0, 150, 45, 4.2, "", "Safe underground parking for mall visitors.", "10 AM - 9 PM", true));
        allParkingList.add(new Parking("3", "Bhatbhateni Maharajgunj", "Maharajgunj, Kathmandu", 27.7347, 85.3323, 40.0, 80, 20, 4.5, "", "Parking area for Bhatbhateni shoppers.", "8 AM - 10 PM", true));
        allParkingList.add(new Parking("4", "New Road Parking", "New Road, Kathmandu", 27.7038, 85.3114, 50.0, 60, 0, 3.8, "", "Public parking area in the busy New Road.", "8 AM - 8 PM", false));
        allParkingList.add(new Parking("5", "Patan Parking Hub", "Patan Durbar Square", 27.6727, 85.3252, 40.0, 50, 15, 4.4, "", "Convenient parking near the historic Patan Durbar Square.", "24/7", true));
        allParkingList.add(new Parking("6", "Pokhara Lakeside Parking", "Lakeside, Pokhara", 28.2095, 83.9587, 30.0, 100, 40, 4.9, "", "Scenic parking area near Phewa Lake.", "24/7", true));
    }

    private void setupRecyclerViews() {
        recommendedAdapter = new ParkingAdapter(new ArrayList<>(allParkingList.subList(0, 3)), this);
        binding.rvRecommended.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecommended.setAdapter(recommendedAdapter);

        nearbyAdapter = new ParkingAdapter(new ArrayList<>(allParkingList), this);
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

    private void filter(String text) {
        List<Parking> filteredList = new ArrayList<>();
        for (Parking item : allParkingList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) || 
                item.getAddress().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        nearbyAdapter.updateList(filteredList);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    updateDistances(location);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
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
        intent.putExtra("parking", parking);
        startActivity(intent);
    }

    @Override
    public void onViewOnMapClick(Parking parking) {
        Intent intent = new Intent(getContext(), ParkingMapActivity.class);
        intent.putExtra("selected_parking", parking);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
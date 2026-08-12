package com.parkshare.frontend.fragments.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.ParkingDetailsActivity;
import com.parkshare.frontend.databinding.FragmentDriverMapBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class DriverMapFragment extends Fragment {

    private FragmentDriverMapBinding binding;
    private MapView mapView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private FusedLocationProviderClient fusedLocationClient;
    private ParkingRepository parkingRepository;
    private final List<Parking> parkingList = new ArrayList<>();
    private Parking selectedParking;
    private double userLat = 27.7172;
    private double userLng = 85.3240;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        binding = FragmentDriverMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parkingRepository = new ParkingRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupMap();
        setupBottomSheet();
        setupListeners();
        checkLocationPermission();
        updateUserInfo();
    }

    private void updateUserInfo() {
        com.parkshare.frontend.utils.SessionManager session = com.parkshare.frontend.utils.SessionManager.getInstance(requireContext());
        String name = session.getFullName();
        if (name != null && !name.isEmpty()) {
            binding.tvGreeting.setText("Good Morning, " + name.split(" ")[0]);
        }
    }

    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(new GeoPoint(userLat, userLng));
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Adjust FABs or other UI if needed
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    private void setupListeners() {
        binding.btnMyLocation.setOnClickListener(v -> getCurrentLocation());
        binding.btnBookNow.setOnClickListener(v -> {
            if (selectedParking != null) {
                try {
                    long id = Long.parseLong(selectedParking.getId());
                    Intent intent = new Intent(getContext(), com.parkshare.frontend.activities.driver.SlotBookingActivity.class);
                    intent.putExtra(com.parkshare.frontend.activities.driver.SlotBookingActivity.EXTRA_PARKING_ID, id);
                    startActivity(intent);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid parking ID", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please select a parking spot first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            getCurrentLocation();
        }
        loadParkingMarkers();
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    GeoPoint userPoint = new GeoPoint(userLat, userLng);
                    mapView.getController().animateTo(userPoint);
                    
                    // Add User Marker
                    Marker userMarker = new Marker(mapView);
                    userMarker.setPosition(userPoint);
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    userMarker.setTitle("You are here");
                    mapView.getOverlays().add(userMarker);
                }
            });
        } catch (SecurityException ignored) {
        }
    }

    private void loadParkingMarkers() {
        parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                if (data != null) {
                    mapView.getOverlays().removeIf(o -> o instanceof Marker && !((Marker) o).getTitle().equals("You are here"));
                    for (ParkingSpaceDto dto : data) {
                        Parking parking = ParkingMapper.fromDto(dto);
                        addParkingMarker(parking);
                    }
                    mapView.invalidate();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Failed to load parking: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addParkingMarker(Parking parking) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(parking.getLatitude(), parking.getLongitude()));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(parking.getName());
        marker.setSubDescription("Rs. " + parking.getPricePerHour());
        
        marker.setOnMarkerClickListener((m, mapView1) -> {
            showParkingDetails(parking);
            mapView1.getController().animateTo(m.getPosition());
            return true;
        });
        
        mapView.getOverlays().add(marker);
    }

    private void showParkingDetails(Parking parking) {
        this.selectedParking = parking;
        binding.tvParkingName.setText(parking.getName());
        binding.tvPrice.setText("Rs. " + parking.getPricePerHour());
        binding.tvRating.setText("★ " + parking.getRating());
        binding.tvDistance.setText(parking.getDistance() != null ? parking.getDistance() : "Near you");
        
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

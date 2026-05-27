package com.parkshare.frontend.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.parkshare.frontend.databinding.ActivityParkingMapBinding;
import com.parkshare.frontend.models.Parking;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class ParkingMapActivity extends AppCompatActivity {

    private ActivityParkingMapBinding binding;
    private MyLocationNewOverlay mLocationOverlay;
    private List<Parking> parkingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityParkingMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMap();
        loadDummyData();
        addMarkers();

        binding.fabBack.setOnClickListener(v -> finish());
        binding.fabMyLocation.setOnClickListener(v -> {
            if (mLocationOverlay.getMyLocation() != null) {
                binding.mapView.getController().animateTo(mLocationOverlay.getMyLocation());
            }
        });

        // Handle intent for specific parking focus
        Parking selectedParking = (Parking) getIntent().getSerializableExtra("selected_parking");
        if (selectedParking != null) {
            GeoPoint point = new GeoPoint(selectedParking.getLatitude(), selectedParking.getLongitude());
            binding.mapView.getController().setCenter(point);
            binding.mapView.getController().setZoom(18.0);
        }
    }

    private void setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(14.0);
        GeoPoint startPoint = new GeoPoint(27.7172, 85.3240); // Kathmandu
        binding.mapView.getController().setCenter(startPoint);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), binding.mapView);
        mLocationOverlay.enableMyLocation();
        binding.mapView.getOverlays().add(mLocationOverlay);
    }

    private void loadDummyData() {
        // This should ideally come from a repository or intent
        parkingList.add(new Parking("1", "Kathmandu Mall Parking", "Kanti Path, Kathmandu", 27.7029, 85.3120, 50.0, 100, 12, 4.5, "", "Secure parking in the heart of the city.", "24/7", true));
        parkingList.add(new Parking("2", "Civil Mall Parking", "Sundhara, Kathmandu", 27.7006, 85.3121, 60.0, 150, 45, 4.2, "", "Underground parking at Civil Mall.", "10 AM - 9 PM", true));
        parkingList.add(new Parking("3", "Durbar Marg Parking", "Durbar Marg, Kathmandu", 27.7107, 85.3168, 80.0, 50, 5, 4.8, "", "Premium parking near Durbar Marg.", "24/7", true));
        // Add more as needed
    }

    private void addMarkers() {
        for (Parking parking : parkingList) {
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(parking.getLatitude(), parking.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(parking.getName());
            marker.setSnippet(parking.getAddress() + "\nPrice: NPR " + parking.getPricePerHour() + "/hr");
            
            marker.setOnMarkerClickListener((m, mapView) -> {
                m.showInfoWindow();
                return true;
            });

            binding.mapView.getOverlays().add(marker);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}
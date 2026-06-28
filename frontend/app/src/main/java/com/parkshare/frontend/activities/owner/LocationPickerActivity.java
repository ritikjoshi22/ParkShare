package com.parkshare.frontend.activities.owner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.frontend.databinding.ActivityLocationPickerBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_ADDRESS = "extra_address";

    private ActivityLocationPickerBinding binding;
    private Marker draggableMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private double selectedLat = 27.7172;
    private double selectedLng = 85.3240;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        selectedLat = getIntent().getDoubleExtra(EXTRA_LAT, selectedLat);
        selectedLng = getIntent().getDoubleExtra(EXTRA_LNG, selectedLng);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupMap();

        binding.fabCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());
        binding.btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    private void setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(17.0);
        GeoPoint start = new GeoPoint(selectedLat, selectedLng);
        binding.mapView.getController().setCenter(start);

        draggableMarker = new Marker(binding.mapView);
        draggableMarker.setPosition(start);
        draggableMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        draggableMarker.setDraggable(true);
        draggableMarker.setTitle("Parking location");
        draggableMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLat = marker.getPosition().getLatitude();
                selectedLng = marker.getPosition().getLongitude();
                reverseGeocode(selectedLat, selectedLng);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });
        binding.mapView.getOverlays().add(draggableMarker);

        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), binding.mapView);
        locationOverlay.enableMyLocation();
        binding.mapView.getOverlays().add(locationOverlay);

        reverseGeocode(selectedLat, selectedLng);
    }

    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                GeoPoint point = new GeoPoint(selectedLat, selectedLng);
                draggableMarker.setPosition(point);
                binding.mapView.getController().animateTo(point);
                reverseGeocode(selectedLat, selectedLng);
            }
        });
    }

    private void reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                String line = address.getAddressLine(0);
                binding.tvAddressPreview.setText(line != null ? line : lat + ", " + lng);
            } else {
                binding.tvAddressPreview.setText(String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng));
            }
        } catch (IOException e) {
            binding.tvAddressPreview.setText(String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng));
        }
    }

    private void confirmLocation() {
        Intent data = new Intent();
        data.putExtra(EXTRA_LAT, selectedLat);
        data.putExtra(EXTRA_LNG, selectedLng);
        CharSequence address = binding.tvAddressPreview.getText();
        if (address != null) {
            data.putExtra(EXTRA_ADDRESS, address.toString());
        }
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}

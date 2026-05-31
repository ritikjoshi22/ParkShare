package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.databinding.ActivityParkingMapBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class ParkingMapActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_SELECTED_PARKING = "selected_parking";

    private ActivityParkingMapBinding binding;
    private MyLocationNewOverlay mLocationOverlay;
    private final List<Parking> parkingList = new ArrayList<>();
    private final ParkingRepository parkingRepository = new ParkingRepository();
    private double userLat = 27.7172;
    private double userLng = 85.3240;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityParkingMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userLat = getIntent().getDoubleExtra(EXTRA_LAT, userLat);
        userLng = getIntent().getDoubleExtra(EXTRA_LNG, userLng);

        setupMap();
        loadParkingFromApi();

        binding.fabBack.setOnClickListener(v -> finish());
        binding.fabMyLocation.setOnClickListener(v -> {
            if (mLocationOverlay.getMyLocation() != null) {
                binding.mapView.getController().animateTo(mLocationOverlay.getMyLocation());
            } else {
                binding.mapView.getController().animateTo(new GeoPoint(userLat, userLng));
            }
        });

        Parking selectedParking = (Parking) getIntent().getSerializableExtra(EXTRA_SELECTED_PARKING);
        if (selectedParking == null) {
            selectedParking = (Parking) getIntent().getSerializableExtra("selected_parking");
        }
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
        binding.mapView.getController().setCenter(new GeoPoint(userLat, userLng));

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), binding.mapView);
        mLocationOverlay.enableMyLocation();
        binding.mapView.getOverlays().add(mLocationOverlay);
    }

    private void loadParkingFromApi() {
        parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                parkingList.clear();
                if (data != null) {
                    for (ParkingSpaceDto dto : data) {
                        parkingList.add(ParkingMapper.fromDto(dto));
                    }
                }
                addMarkers();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ParkingMapActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkers() {
        for (Parking parking : parkingList) {
            Marker marker = new Marker(binding.mapView);
            marker.setPosition(new GeoPoint(parking.getLatitude(), parking.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(parking.getName());
            marker.setSnippet(parking.getAddress() + "\nNPR " + parking.getPricePerHour() + "/hr");

            marker.setOnMarkerClickListener((m, mapView) -> {
                m.showInfoWindow();
                startActivity(new Intent(ParkingMapActivity.this, ParkingDetailsActivity.class)
                        .putExtra(ParkingDetailsActivity.EXTRA_PARKING_ID, Long.parseLong(parking.getId())));
                return true;
            });

            binding.mapView.getOverlays().add(marker);
        }
        binding.mapView.invalidate();
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

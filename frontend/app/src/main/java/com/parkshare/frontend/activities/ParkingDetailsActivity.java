package com.parkshare.frontend.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.databinding.ActivityParkingDetailsBinding;
import com.parkshare.frontend.models.Parking;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class ParkingDetailsActivity extends AppCompatActivity {

    private ActivityParkingDetailsBinding binding;
    private Parking parking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityParkingDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parking = (Parking) getIntent().getSerializableExtra("parking");

        if (parking != null) {
            setupUI();
            setupMap();
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.btnNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + parking.getLatitude() + "," + parking.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBook.setOnClickListener(v -> 
            Toast.makeText(this, "Booking feature coming soon", Toast.LENGTH_LONG).show());

        binding.btnFullMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParkingMapActivity.class);
            intent.putExtra("selected_parking", parking);
            startActivity(intent);
        });
    }

    private void setupUI() {
        binding.tvName.setText(parking.getName());
        binding.tvAddress.setText(parking.getAddress());
        binding.tvPrice.setText("$" + String.format("%.2f", parking.getPricePerHour()) + "/hr");
        binding.tvRating.setText(String.valueOf(parking.getRating()));
        binding.tvDistance.setText(parking.getDistance() != null ? parking.getDistance() : "Distance unavailable");
        binding.tvSlots.setText(parking.getAvailableSlots() + " / " + parking.getTotalSlots() + " slots available");
        binding.tvDescription.setText(parking.getDescription());
        binding.tvHours.setText(parking.getOpeningHours());
        
        if (parking.isOpen()) {
            binding.tvStatus.setText("Open Now");
            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvStatus.setText("Closed");
            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void setupMap() {
        binding.mapPreview.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapPreview.setMultiTouchControls(false);
        binding.mapPreview.getController().setZoom(17.0);
        
        GeoPoint startPoint = new GeoPoint(parking.getLatitude(), parking.getLongitude());
        binding.mapPreview.getController().setCenter(startPoint);

        Marker startMarker = new Marker(binding.mapPreview);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(parking.getName());
        binding.mapPreview.getOverlays().add(startMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapPreview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapPreview.onPause();
    }
}
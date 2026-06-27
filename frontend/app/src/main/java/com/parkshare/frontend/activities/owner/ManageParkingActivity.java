package com.parkshare.frontend.activities.owner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityManageParkingBinding;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.RoleRouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageParkingActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivityManageParkingBinding binding;
    private ParkingRepository parkingRepository;
    private long parkingId = -1;
    private FusedLocationProviderClient fusedLocationClient;
    private final List<Uri> pendingImageUris = new ArrayList<>();

    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    binding.etLat.setText(String.valueOf(data.getDoubleExtra(LocationPickerActivity.EXTRA_LAT, 0)));
                    binding.etLng.setText(String.valueOf(data.getDoubleExtra(LocationPickerActivity.EXTRA_LNG, 0)));
                    String address = data.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS);
                    if (address != null && !address.isEmpty()) {
                        binding.etAddress.setText(address);
                    }
                }
            });

    private final ActivityResultLauncher<String[]> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null) {
                    pendingImageUris.clear();
                    pendingImageUris.addAll(uris);
                    Toast.makeText(this, pendingImageUris.size() + " photo(s) selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleRouter.isRoleAllowed(this, "owner")) {
            finish();
            return;
        }
        binding = ActivityManageParkingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        parkingRepository = new ParkingRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        parkingId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);

        if (parkingId > 0) {
            binding.toolbar.setTitle(R.string.edit_parking);
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadParking();
        } else {
            binding.toolbar.setTitle(R.string.add_parking);
        }

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
        binding.btnPickOnMap.setOnClickListener(v -> openMapPicker());
        binding.btnUseCurrentLocation.setOnClickListener(v -> useCurrentLocation());
        binding.btnAddPhotos.setOnClickListener(v -> photoPickerLauncher.launch(new String[]{"image/*"}));
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        try {
            intent.putExtra(LocationPickerActivity.EXTRA_LAT, Double.parseDouble(text(binding.etLat)));
            intent.putExtra(LocationPickerActivity.EXTRA_LNG, Double.parseDouble(text(binding.etLng)));
        } catch (NumberFormatException ignored) {
        }
        locationPickerLauncher.launch(intent);
    }

    private void useCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                binding.etLat.setText(String.valueOf(location.getLatitude()));
                binding.etLng.setText(String.valueOf(location.getLongitude()));
            } else {
                Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadParking() {
        parkingRepository.getById(parkingId, new RepositoryCallback<ParkingSpaceDto>() {
            @Override
            public void onSuccess(ParkingSpaceDto data) {
                if (data == null) {
                    return;
                }
                binding.etName.setText(data.getParkingName());
                binding.etAddress.setText(data.getAddress());
                binding.etDescription.setText(data.getDescription());
                binding.etPrice.setText(String.valueOf(data.getPricePerHour()));
                binding.etTotalSlots.setText(String.valueOf(data.getTotalSlots()));
                binding.etAvailableSlots.setText(String.valueOf(data.getAvailableSlots()));
                binding.etLat.setText(String.valueOf(data.getLatitude()));
                binding.etLng.setText(String.valueOf(data.getLongitude()));
                binding.etOpen.setText(data.getOpeningTime() != null ? data.getOpeningTime().substring(0, 5) : "08:00");
                binding.etClose.setText(data.getClosingTime() != null ? data.getClosingTime().substring(0, 5) : "20:00");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageParkingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void save() {
        String name = text(binding.etName);
        String address = text(binding.etAddress);
        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        Double price = parseDouble(text(binding.etPrice));
        Integer totalSlots = parseInt(text(binding.etTotalSlots));
        Integer availableSlots = parseInt(text(binding.etAvailableSlots));
        Double lat = parseDouble(text(binding.etLat));
        Double lng = parseDouble(text(binding.etLng));
        if (price == null || totalSlots == null || availableSlots == null || lat == null || lng == null) {
            Toast.makeText(this, R.string.invalid_number_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("parking_name", name);
        body.put("address", address);
        body.put("description", text(binding.etDescription));
        body.put("price_per_hour", price);
        body.put("total_slots", totalSlots);
        body.put("available_slots", availableSlots);
        body.put("latitude", lat);
        body.put("longitude", lng);
        body.put("vehicle_type", "both");
        body.put("opening_time", text(binding.etOpen));
        body.put("closing_time", text(binding.etClose));

        binding.btnSave.setEnabled(false);
        if (parkingId > 0) {
            parkingRepository.update(parkingId, body, new RepositoryCallback<ParkingSpaceDto>() {
                @Override
                public void onSuccess(ParkingSpaceDto data) {
                    uploadPhotosIfNeeded(parkingId);
                    Toast.makeText(ManageParkingActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(ManageParkingActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            parkingRepository.create(body, new RepositoryCallback<ParkingSpaceDto>() {
                @Override
                public void onSuccess(ParkingSpaceDto data) {
                    if (data != null) {
                        uploadPhotosIfNeeded(data.getId());
                    }
                    Toast.makeText(ManageParkingActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(ManageParkingActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void uploadPhotosIfNeeded(long id) {
        if (!pendingImageUris.isEmpty()) {
            parkingRepository.uploadImages(id, pendingImageUris, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_parking)
                .setMessage(R.string.delete_parking_confirm)
                .setPositiveButton(R.string.delete, (d, w) ->
                        parkingRepository.delete(parkingId, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(ManageParkingActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String text(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Nullable
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

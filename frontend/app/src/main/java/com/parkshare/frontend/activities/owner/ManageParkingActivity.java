package com.parkshare.frontend.activities.owner;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityManageParkingBinding;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.RoleRouter;
import com.parkshare.frontend.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class ManageParkingActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivityManageParkingBinding binding;
    private ParkingRepository parkingRepository;
    private long parkingId = -1;

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
        Map<String, Object> body = new HashMap<>();
        body.put("parking_name", text(binding.etName));
        body.put("address", text(binding.etAddress));
        body.put("description", text(binding.etDescription));
        body.put("price_per_hour", Double.parseDouble(text(binding.etPrice)));
        body.put("total_slots", Integer.parseInt(text(binding.etTotalSlots)));
        body.put("available_slots", Integer.parseInt(text(binding.etAvailableSlots)));
        body.put("latitude", Double.parseDouble(text(binding.etLat)));
        body.put("longitude", Double.parseDouble(text(binding.etLng)));
        body.put("vehicle_type", "both");
        body.put("opening_time", text(binding.etOpen));
        body.put("closing_time", text(binding.etClose));

        binding.btnSave.setEnabled(false);
        if (parkingId > 0) {
            parkingRepository.update(parkingId, body, new RepositoryCallback<ParkingSpaceDto>() {
                @Override
                public void onSuccess(ParkingSpaceDto data) {
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
}

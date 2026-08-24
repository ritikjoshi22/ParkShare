package com.parkshare.frontend.activities.owner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.parkshare.api.models.ParkingTechnicianDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.repository.OwnerRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageTechniciansActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private OwnerRepository ownerRepository;
    private long parkingSpaceId;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TechnicianAdapter adapter;
    private final List<ParkingTechnicianDto> technicians = new ArrayList<>();

    public static Intent intent(Context context, long parkingSpaceId) {
        Intent intent = new Intent(context, ManageTechniciansActivity.class);
        intent.putExtra(EXTRA_PARKING_ID, parkingSpaceId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.getInstance(this).hasOwnerCapability()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_manage_technicians);
        parkingSpaceId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);
        if (parkingSpaceId < 0) {
            finish();
            return;
        }
        ownerRepository = new OwnerRepository();
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.rvTechnicians);
        tvEmpty = findViewById(R.id.tvEmpty);
        adapter = new TechnicianAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showTechnicianDialog(null));
        loadTechnicians();
    }

    private void loadTechnicians() {
        ownerRepository.fetchTechnicians(parkingSpaceId, new RepositoryCallback<List<ParkingTechnicianDto>>() {
            @Override
            public void onSuccess(List<ParkingTechnicianDto> data) {
                technicians.clear();
                if (data != null) {
                    technicians.addAll(data);
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(technicians.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageTechniciansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTechnicianDialog(ParkingTechnicianDto existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_technician_form, null, false);
        TextInputEditText name = form.findViewById(R.id.etName);
        TextInputEditText phone = form.findViewById(R.id.etPhone);
        TextInputEditText specialization = form.findViewById(R.id.etSpecialization);
        if (existing != null) {
            name.setText(existing.getName());
            phone.setText(existing.getPhone());
            specialization.setText(existing.getSpecialization());
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.add_technician : R.string.edit_technician)
                .setView(form)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("name", text(name));
                    body.put("phone", text(phone));
                    body.put("specialization", text(specialization).isEmpty() ? "general_mechanic" : text(specialization));
                    body.put("availability_status", "available");
                    if (existing == null) {
                        ownerRepository.addTechnician(parkingSpaceId, body, saveCallback());
                    } else {
                        ownerRepository.updateTechnician(parkingSpaceId, existing.getId(), body, saveCallback());
                    }
                })
                .show();
    }

    private RepositoryCallback<ParkingTechnicianDto> saveCallback() {
        return new RepositoryCallback<ParkingTechnicianDto>() {
            @Override
            public void onSuccess(ParkingTechnicianDto data) {
                loadTechnicians();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageTechniciansActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };
    }

    private String text(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parking_technician, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ParkingTechnicianDto tech = technicians.get(position);
            holder.name.setText(tech.getName());
            holder.meta.setText(tech.getSpecialization() + " • " + tech.getAvailabilityStatus()
                    + (tech.isPrimary() ? " • Primary" : ""));
            holder.phone.setText(tech.getPhone());
            holder.itemView.setOnClickListener(v -> showTechnicianDialog(tech));
            holder.phone.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tech.getPhone()));
                startActivity(call);
            });
        }

        @Override
        public int getItemCount() {
            return technicians.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView meta;
            final TextView phone;

            Holder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvName);
                meta = itemView.findViewById(R.id.tvMeta);
                phone = itemView.findViewById(R.id.tvPhone);
            }
        }
    }
}

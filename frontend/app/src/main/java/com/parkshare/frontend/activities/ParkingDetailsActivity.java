package com.parkshare.frontend.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.bumptech.glide.Glide;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.parkshare.api.models.FavoriteDto;
import com.parkshare.api.models.ParkingImageDto;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.api.models.ParkingTechnicianDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.ParkingImagesAdapter;
import com.parkshare.frontend.databinding.ActivityParkingDetailsBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.FavoriteRepository;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.repository.ReviewRepository;
import com.parkshare.frontend.activities.driver.SlotBookingActivity;
import com.parkshare.frontend.utils.MapsNavigationHelper;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivityParkingDetailsBinding binding;
    private Parking parking;
    private long parkingId;
    private Long favoriteId;
    private final ParkingRepository parkingRepository = new ParkingRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private TechnicianAdapter techAdapter;
    private final List<ParkingTechnicianDto> technicians = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityParkingDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parkingId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);
        if (parkingId < 0) {
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnNavigate.setOnClickListener(v -> openMaps());
        binding.btnBook.setOnClickListener(v -> {
            if (parking != null) {
                startActivity(new Intent(this, SlotBookingActivity.class)
                        .putExtra(SlotBookingActivity.EXTRA_PARKING_ID, parkingId));
            }
        });
        binding.btnFullMap.setOnClickListener(v -> {
            if (parking != null) {
                Intent intent = new Intent(this, ParkingMapActivity.class);
                intent.putExtra(ParkingMapActivity.EXTRA_SELECTED_PARKING, parking);
                intent.putExtra(ParkingMapActivity.EXTRA_LAT, parking.getLatitude());
                intent.putExtra(ParkingMapActivity.EXTRA_LNG, parking.getLongitude());
                startActivity(intent);
            }
        });

        loadParkingDetails();
        loadTechnicians();
        checkFavoriteStatus();
    }

    private void loadTechnicians() {
        binding.rvTechnicians.setLayoutManager(new LinearLayoutManager(this));
        techAdapter = new TechnicianAdapter(technicians);
        binding.rvTechnicians.setAdapter(techAdapter);

        parkingRepository.getTechnicians(parkingId, new RepositoryCallback<List<ParkingTechnicianDto>>() {
            @Override
            public void onSuccess(List<ParkingTechnicianDto> data) {
                technicians.clear();
                if (data != null && !data.isEmpty()) {
                    technicians.addAll(data);
                    binding.tvTechniciansHeader.setVisibility(View.VISIBLE);
                    binding.rvTechnicians.setVisibility(View.VISIBLE);
                } else {
                    binding.tvTechniciansHeader.setVisibility(View.GONE);
                    binding.rvTechnicians.setVisibility(View.GONE);
                }
                techAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadParkingDetails() {
        binding.progressOverlay.setVisibility(View.VISIBLE);
        parkingRepository.getById(parkingId, new RepositoryCallback<ParkingSpaceDto>() {
            @Override
            public void onSuccess(ParkingSpaceDto data) {
                binding.progressOverlay.setVisibility(View.GONE);
                parking = ParkingMapper.fromDto(data);
                setupUI(data);
                setupMap();
                loadReviews();
            }

            @Override
            public void onError(String message) {
                binding.progressOverlay.setVisibility(View.GONE);
                Toast.makeText(ParkingDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupUI(ParkingSpaceDto data) {
        binding.tvName.setText(parking.getName());
        binding.tvAddress.setText(parking.getAddress());
        binding.tvPrice.setText(String.format(Locale.getDefault(), "NPR %.0f/hr", parking.getPricePerHour()));
        binding.tvRating.setText(String.valueOf(parking.getRating()));
        binding.tvDistance.setText(parking.getDistance() != null ? parking.getDistance() : "—");
        binding.tvSlots.setText(parking.getAvailableSlots() + " / " + parking.getTotalSlots() + " slots available");
        binding.tvDescription.setText(parking.getDescription());
        binding.tvHours.setText(parking.getOpeningHours());

        if (parking.isOpen()) {
            binding.tvStatus.setText("Open Now");
            binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvStatus.setText("Closed");
            binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        setupImageSlider(data.getImages());

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        
        binding.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID, data.getOwnerId());
            intent.putExtra(ChatActivity.EXTRA_NAME, parking.getName());
            startActivity(intent);
        });
    }

    private void setupImageSlider(List<ParkingImageDto> images) {
        ParkingImagesAdapter adapter = new ParkingImagesAdapter();
        java.util.List<String> urls = new java.util.ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (ParkingImageDto img : images) {
                urls.add(img.getImageUrl());
            }
        } else if (parking.getImageUrl() != null) {
            urls.add(parking.getImageUrl());
        }

        if (urls.isEmpty()) {
            binding.vpImages.setVisibility(View.GONE);
            binding.tabDots.setVisibility(View.GONE);
            return;
        }

        adapter.setItems(urls);
        binding.vpImages.setAdapter(adapter);
        
        if (urls.size() > 1) {
            new TabLayoutMediator(binding.tabDots, binding.vpImages, (tab, position) -> {}).attach();
        } else {
            binding.tabDots.setVisibility(View.GONE);
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

    private void loadReviews() {
        reviewRepository.getForParking(parkingId, new RepositoryCallback<List<com.parkshare.api.models.ReviewDto>>() {
            @Override
            public void onSuccess(List<com.parkshare.api.models.ReviewDto> data) {
                if (data != null && !data.isEmpty() && binding.tvReviews != null) {
                    StringBuilder sb = new StringBuilder();
                    int limit = Math.min(3, data.size());
                    for (int i = 0; i < limit; i++) {
                        com.parkshare.api.models.ReviewDto r = data.get(i);
                        sb.append("★").append(r.getRating()).append(" ");
                        if (r.getReviewText() != null) {
                            sb.append(r.getReviewText());
                        }
                        sb.append("\n");
                    }
                    binding.tvReviews.setText(sb.toString().trim());
                    binding.tvReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                // optional
            }
        });
    }

    private void checkFavoriteStatus() {
        favoriteRepository.getFavorites(1, new RepositoryCallback<List<FavoriteDto>>() {
            @Override
            public void onSuccess(List<FavoriteDto> data) {
                favoriteId = null;
                if (data != null) {
                    for (FavoriteDto f : data) {
                        if (f.getParkingSpaceId() == parkingId) {
                            favoriteId = f.getId();
                            break;
                        }
                    }
                }
                updateFavoriteButton();
            }

            @Override
            public void onError(String message) {
                updateFavoriteButton();
            }
        });
    }

    private void updateFavoriteButton() {
        if (binding.btnFavorite == null) {
            return;
        }
        boolean isFavorite = favoriteId != null;
        binding.btnFavorite.setText(isFavorite ? R.string.remove_favorite : R.string.add_favorite);
    }

    private void toggleFavorite() {
        if (favoriteId != null) {
            favoriteRepository.removeFavorite(favoriteId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    favoriteId = null;
                    updateFavoriteButton();
                    Toast.makeText(ParkingDetailsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ParkingDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            favoriteRepository.addFavorite(parkingId, new RepositoryCallback<FavoriteDto>() {
                @Override
                public void onSuccess(FavoriteDto data) {
                    favoriteId = data.getId();
                    updateFavoriteButton();
                    Toast.makeText(ParkingDetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ParkingDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void openMaps() {
        if (parking == null) {
            return;
        }
        MapsNavigationHelper.openNavigation(this, parking.getLatitude(), parking.getLongitude(),
                parking.getName());
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

    private static class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.Holder> {
        private final List<ParkingTechnicianDto> items;

        TechnicianAdapter(List<ParkingTechnicianDto> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parking_technician, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ParkingTechnicianDto tech = items.get(position);
            holder.name.setText(tech.getName());
            holder.meta.setText(tech.getSpecialization() + " • " + tech.getAvailabilityStatus());
            holder.phone.setText(tech.getPhone());
            holder.phone.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tech.getPhone()));
                v.getContext().startActivity(call);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
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

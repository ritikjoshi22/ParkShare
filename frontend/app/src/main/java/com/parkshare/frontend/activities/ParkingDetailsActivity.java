package com.parkshare.frontend.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.parkshare.api.models.FavoriteDto;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityParkingDetailsBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.repository.FavoriteRepository;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.repository.ReviewRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ParkingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivityParkingDetailsBinding binding;
    private Parking parking;
    private long parkingId;
    private Long favoriteId;
    private final ParkingRepository parkingRepository = new ParkingRepository();
    private final BookingRepository bookingRepository = new BookingRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();

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
        binding.btnBook.setOnClickListener(v -> createBooking());
        binding.btnFullMap.setOnClickListener(v -> {
            if (parking != null) {
                Intent intent = new Intent(this, ParkingMapActivity.class);
                intent.putExtra("selected_parking", parking);
                startActivity(intent);
            }
        });

        loadParkingDetails();
        checkFavoriteStatus();
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

        if (parking.getImageUrl() != null && !parking.getImageUrl().isEmpty()) {
            Glide.with(this).load(parking.getImageUrl()).into(binding.ivHeader);
        }

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
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

    private void createBooking() {
        if (parking.getAvailableSlots() <= 0) {
            Toast.makeText(this, "No slots available", Toast.LENGTH_SHORT).show();
            return;
        }

        OffsetDateTime start = OffsetDateTime.now().plusHours(1);
        OffsetDateTime end = start.plusHours(2);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        binding.btnBook.setEnabled(false);
        bookingRepository.createBooking(parkingId, start.format(formatter), end.format(formatter),
                new RepositoryCallback<com.parkshare.api.models.BookingDto>() {
                    @Override
                    public void onSuccess(com.parkshare.api.models.BookingDto data) {
                        binding.btnBook.setEnabled(true);
                        Toast.makeText(ParkingDetailsActivity.this, R.string.booking_success, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String message) {
                        binding.btnBook.setEnabled(true);
                        Toast.makeText(ParkingDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openMaps() {
        if (parking == null) {
            return;
        }
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + parking.getLatitude() + "," + parking.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show();
        }
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

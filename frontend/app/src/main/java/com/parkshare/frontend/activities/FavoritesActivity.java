package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.FavoriteDto;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.ActivityFavoritesBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.FavoriteRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements ParkingAdapter.OnParkingActionListener {

    private ActivityFavoritesBinding binding;
    private ParkingAdapter adapter;
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        adapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        favoriteRepository.getFavorites(1, new RepositoryCallback<List<FavoriteDto>>() {
            @Override
            public void onSuccess(List<FavoriteDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                List<Parking> parkings = new ArrayList<>();
                if (data != null) {
                    for (FavoriteDto f : data) {
                        if (f.getParkingSpace() != null) {
                            parkings.add(ParkingMapper.fromDto(f.getParkingSpace()));
                        }
                    }
                }
                adapter.updateList(parkings);
                binding.tvEmpty.setVisibility(parkings.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onParkingClick(Parking parking) {
        Intent intent = new Intent(this, ParkingDetailsActivity.class);
        intent.putExtra(ParkingDetailsActivity.EXTRA_PARKING_ID, Long.parseLong(parking.getId()));
        startActivity(intent);
    }

    @Override
    public void onViewOnMapClick(Parking parking) {
        Intent intent = new Intent(this, ParkingMapActivity.class);
        intent.putExtra("selected_parking", parking);
        startActivity(intent);
    }
}

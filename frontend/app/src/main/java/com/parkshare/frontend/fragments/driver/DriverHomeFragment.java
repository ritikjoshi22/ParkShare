package com.parkshare.frontend.fragments.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parkshare.api.models.ParkingSpaceDto;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.parkshare.frontend.R;
import com.parkshare.frontend.utils.LoadingHelper;
import com.parkshare.frontend.utils.ShimmerUi;
import com.parkshare.frontend.activities.FavoritesActivity;
import com.parkshare.frontend.activities.ParkingDetailsActivity;
import com.parkshare.frontend.activities.ParkingMapActivity;
import com.parkshare.frontend.adapters.ParkingAdapter;
import com.parkshare.frontend.databinding.FragmentDriverHomeBinding;
import com.parkshare.frontend.models.Parking;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.ParkingMapper;
import com.parkshare.frontend.utils.RepositoryCallback;

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.SessionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DriverHomeFragment extends Fragment implements ParkingAdapter.OnParkingActionListener {

    private FragmentDriverHomeBinding binding;
    private ParkingAdapter recommendedAdapter;
    private ParkingAdapter nearbyAdapter;
    private final List<Parking> allParkingList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private ParkingRepository parkingRepository;
    private double userLat = 27.7172;
    private double userLng = 85.3240;
    private ShimmerFrameLayout shimmerLayout;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDriverHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parkingRepository = new ParkingRepository();
        sessionManager = SessionManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupGreeting();

        recommendedAdapter = new ParkingAdapter(new ArrayList<>(), this, true);
        binding.rvRecommended.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecommended.setAdapter(recommendedAdapter);

        nearbyAdapter = new ParkingAdapter(new ArrayList<>(), this);
        binding.rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNearby.setAdapter(nearbyAdapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        ShimmerUi.prepareListSkeleton(view, R.layout.shimmer_parking_item, 4);

        binding.swipeRefresh.setOnRefreshListener(this::loadParkingData);
        binding.btnRetry.setOnClickListener(v -> loadParkingData());
        
        binding.ivSos.setOnClickListener(v -> 
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.driver_sos));
        
        binding.btnSeeAll.setOnClickListener(v -> 
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.driver_map));

        binding.btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(getContext(), FavoritesActivity.class)));

        binding.btnFindParking.setOnClickListener(v -> 
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.driver_map));

        checkLocationPermission();
        updateActiveSessionCard();
    }

    private void updateActiveSessionCard() {
        new BookingRepository().getActiveBooking(new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                if (data != null) {
                    binding.tvSessionTitle.setText("Active Booking");
                    binding.tvSessionDesc.setText("You have an active booking at " + data.getParkingSpace().getParkingName());
                    binding.btnFindParking.setText("View Booking");
                    binding.btnFindParking.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putLong("booking_id", data.getId());
                        androidx.navigation.Navigation.findNavController(binding.getRoot())
                                .navigate(R.id.driver_bookings, args); // Or specific detail fragment
                    });
                } else {
                    binding.tvSessionTitle.setText(R.string.no_active_session);
                    binding.tvSessionDesc.setText(R.string.active_session_desc);
                    binding.btnFindParking.setText(R.string.find_my_parking);
                    binding.btnFindParking.setOnClickListener(v ->
                            androidx.navigation.Navigation.findNavController(binding.getRoot()).navigate(R.id.driver_map));
                }
            }

            @Override
            public void onError(String message) {
                // Keep default "No active session" look
            }
        });
    }

    private void setupGreeting() {
        String name = sessionManager.getFullName();
        if (name == null || name.isEmpty()) {
            name = "User";
        } else {
            // Get first name
            name = name.split(" ")[0];
        }

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = getString(R.string.good_morning, name);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = getString(R.string.good_afternoon, name);
        } else {
            greeting = getString(R.string.good_evening, name);
        }
        binding.tvGreeting.setText(greeting);
    }

    private void loadParkingData() {
        binding.layoutError.setVisibility(View.GONE);
        boolean isRefreshing = binding.swipeRefresh.isRefreshing();

        if (!isRefreshing) {
            LoadingHelper.showShimmer(shimmerLayout, binding.progressBar);
        }

        parkingRepository.getNearby(userLat, userLng, 1, new RepositoryCallback<List<ParkingSpaceDto>>() {
            @Override
            public void onSuccess(List<ParkingSpaceDto> data) {
                if (data != null && !data.isEmpty()) {
                    displayParkingData(data);
                } else {
                    // Fallback to all parking if nearby is empty
                    parkingRepository.getAll(1, userLat, userLng, new RepositoryCallback<List<ParkingSpaceDto>>() {
                        @Override
                        public void onSuccess(List<ParkingSpaceDto> data) {
                            displayParkingData(data);
                        }

                        @Override
                        public void onError(String message) {
                            handleLoadError(message);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                handleLoadError(message);
            }
        });
    }

    private void displayParkingData(List<ParkingSpaceDto> data) {
        LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
        binding.swipeRefresh.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutError.setVisibility(View.GONE);

        allParkingList.clear();
        if (data != null) {
            for (ParkingSpaceDto dto : data) {
                Parking parking = ParkingMapper.fromDto(dto);
                if (dto.getDistanceKm() != null) {
                    parking.setDistance(String.format("%.1f km away", dto.getDistanceKm()));
                } else {
                    float[] results = new float[1];
                    Location.distanceBetween(userLat, userLng, parking.getLatitude(), parking.getLongitude(), results);
                    float m = results[0];
                    parking.setDistance(m >= 1000 ? String.format("%.1f km away", m / 1000f) : (int) m + " m away");
                }
                allParkingList.add(parking);
            }
        }

        // IMPROVED RECOMMENDATION LOGIC: Sort by rating (descending)
        List<Parking> sortedList = new ArrayList<>(allParkingList);
        Collections.sort(sortedList, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));

        List<Parking> recommended = sortedList.size() > 5
                ? new ArrayList<>(sortedList.subList(0, 5))
                : new ArrayList<>(sortedList);

        recommendedAdapter.updateList(recommended);
        nearbyAdapter.updateList(new ArrayList<>(allParkingList));

        if (allParkingList.isEmpty()) {
            binding.layoutError.setVisibility(View.VISIBLE);
            binding.tvError.setText(R.string.no_parking_found);
        }
    }

    private void handleLoadError(String message) {
        LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
        binding.swipeRefresh.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setRefreshing(false);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
    }

    private void filter(String text) {
        List<Parking> filtered = new ArrayList<>();
        String q = text != null ? text.toLowerCase() : "";
        for (Parking p : allParkingList) {
            if (p.getName().toLowerCase().contains(q) || p.getAddress().toLowerCase().contains(q)) {
                filtered.add(p);
            }
        }
        nearbyAdapter.updateList(filtered);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            getCurrentLocation();
        }
        loadParkingData();
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    
                    if (allParkingList.isEmpty()) {
                        loadParkingData();
                    } else {
                        updateDistances();
                    }
                }
            });
        } catch (SecurityException ignored) {
        }
    }

    private void updateDistances() {
        for (Parking parking : allParkingList) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLng, parking.getLatitude(), parking.getLongitude(), results);
            float m = results[0];
            parking.setDistance(m >= 1000 ? String.format("%.1f km away", m / 1000f) : (int) m + " m away");
        }
        recommendedAdapter.notifyDataSetChanged();
        nearbyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onParkingClick(Parking parking) {
        try {
            long id = Long.parseLong(parking.getId());
            Intent intent = new Intent(getContext(), ParkingDetailsActivity.class);
            intent.putExtra(ParkingDetailsActivity.EXTRA_PARKING_ID, id);
            startActivity(intent);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void onViewOnMapClick(Parking parking) {
        Intent intent = new Intent(getContext(), ParkingMapActivity.class);
        intent.putExtra(ParkingMapActivity.EXTRA_LAT, userLat);
        intent.putExtra(ParkingMapActivity.EXTRA_LNG, userLng);
        intent.putExtra(ParkingMapActivity.EXTRA_SELECTED_PARKING, parking);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

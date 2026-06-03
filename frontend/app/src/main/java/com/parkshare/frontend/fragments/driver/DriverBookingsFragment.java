package com.parkshare.frontend.fragments.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.BookingAdapter;
import com.parkshare.frontend.databinding.FragmentDriverBookingsBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverBookingsFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private FragmentDriverBookingsBinding binding;
    private BookingAdapter adapter;
    private List<BookingDto> allBookings = new ArrayList<>();
    private String filterStatus = "active";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDriverBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new BookingAdapter(this);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(adapter);
        binding.btnRetry.setOnClickListener(v -> loadBookings());
        binding.btnExplore.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.driver_home));

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_active));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_upcoming));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_completed));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        filterStatus = "pending";
                        break;
                    case 2:
                        filterStatus = "completed";
                        break;
                    default:
                        filterStatus = "active";
                        break;
                }
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadBookings();
    }

    private void loadBookings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                allBookings = data != null ? data : new ArrayList<>();
                applyFilter();
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvError.setText(message);
            }
        });
    }

    private void applyFilter() {
        List<BookingDto> filtered = new ArrayList<>();
        for (BookingDto b : allBookings) {
            String status = b.getBookingStatus();
            if ("active".equals(filterStatus)) {
                if ("confirmed".equals(status) || "pending".equals(status)) {
                    filtered.add(b);
                }
            } else if (filterStatus.equals(status)
                    || ("active".equals(filterStatus) && "confirmed".equals(status))) {
                filtered.add(b);
            } else if ("pending".equals(filterStatus) && "pending".equals(status)) {
                filtered.add(b);
            } else if ("completed".equals(filterStatus) && ("completed".equals(status) || "cancelled".equals(status))) {
                filtered.add(b);
            }
        }
        if ("completed".equals(filterStatus)) {
            filtered.clear();
            for (BookingDto b : allBookings) {
                if ("completed".equals(b.getBookingStatus()) || "cancelled".equals(b.getBookingStatus())) {
                    filtered.add(b);
                }
            }
        } else if ("pending".equals(filterStatus)) {
            filtered.clear();
            for (BookingDto b : allBookings) {
                if ("pending".equals(b.getBookingStatus())) {
                    filtered.add(b);
                }
            }
        } else {
            filtered.clear();
            for (BookingDto b : allBookings) {
                if ("confirmed".equals(b.getBookingStatus())) {
                    filtered.add(b);
                }
            }
        }
        adapter.setItems(filtered);
        boolean empty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCancel(BookingDto booking) {
        new BookingRepository().cancelBooking(booking.getId(), new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                Toast.makeText(requireContext(), R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
                loadBookings();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

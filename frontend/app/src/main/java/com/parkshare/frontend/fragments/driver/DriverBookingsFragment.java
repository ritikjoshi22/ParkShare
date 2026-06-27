package com.parkshare.frontend.fragments.driver;

import android.content.Intent;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.parkshare.frontend.R;
import com.parkshare.api.models.ExtensionOptionsDto;
import com.parkshare.frontend.activities.BookingPaymentActivity;
import com.parkshare.frontend.activities.BookingQrActivity;
import com.parkshare.frontend.adapters.BookingAdapter;
import com.parkshare.frontend.utils.LoadingHelper;
import com.parkshare.frontend.utils.ShimmerUi;
import com.parkshare.frontend.databinding.FragmentDriverBookingsBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverBookingsFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private FragmentDriverBookingsBinding binding;
    private BookingAdapter adapter;
    private ShimmerFrameLayout shimmerLayout;
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
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        ShimmerUi.prepareListSkeleton(view, R.layout.shimmer_booking_item, 5);

        adapter = new BookingAdapter(this);
        adapter.setShowQrButton(true);
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
        LoadingHelper.showShimmer(shimmerLayout, binding.progressBar);
        binding.layoutError.setVisibility(View.GONE);
        binding.rvBookings.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                allBookings = data != null ? data : new ArrayList<>();
                applyFilter();
            }

            @Override
            public void onError(String message) {
                LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
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
                if ("confirmed".equals(status) || "pending".equals(status) || "checked_in".equals(status)) {
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
                if ("confirmed".equals(b.getBookingStatus()) || "checked_in".equals(b.getBookingStatus()) || "pending".equals(b.getBookingStatus())) {
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
    public void onViewQr(BookingDto booking) {
        startActivity(BookingQrActivity.intent(requireContext(), booking));
    }

    @Override
    public void onExtend(BookingDto booking) {
        new BookingRepository().getExtensionOptions(booking.getId(), new RepositoryCallback<ExtensionOptionsDto>() {
            @Override
            public void onSuccess(ExtensionOptionsDto options) {
                if (options == null) {
                    Toast.makeText(requireContext(), "Unable to fetch extension options", Toast.LENGTH_SHORT).show();
                    return;
                }
                BookingExtensionBottomSheet sheet = BookingExtensionBottomSheet.newInstance(booking, options, () -> {
                    loadBookings();
                });
                sheet.show(getChildFragmentManager(), "extension_sheet");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPay(BookingDto booking) {
        Intent intent = new Intent(requireContext(), BookingPaymentActivity.class);
        intent.putExtra(BookingPaymentActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(BookingPaymentActivity.EXTRA_AMOUNT, booking.getAmountDue());
        intent.putExtra(BookingPaymentActivity.EXTRA_TYPE, "balance");
        startActivity(intent);
    }

    @Override
    public void onChat(BookingDto booking) {
        if (booking.getParkingSpace() == null) return;
        Intent intent = new Intent(requireContext(), com.parkshare.frontend.activities.ChatActivity.class);
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_RECEIVER_ID, booking.getParkingSpace().getOwnerId());
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_NAME, booking.getParkingSpace().getParkingName());
        startActivity(intent);
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

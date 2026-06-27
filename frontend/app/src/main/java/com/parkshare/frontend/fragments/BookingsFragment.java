package com.parkshare.frontend.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.BookingAdapter;
import com.parkshare.frontend.databinding.FragmentBookingsBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.List;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private FragmentBookingsBinding binding;
    private BookingRepository bookingRepository;
    private BookingAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingRepository = new BookingRepository();
        adapter = new BookingAdapter(this);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookings.setAdapter(adapter);

        binding.btnExplore.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigation_home));
        binding.btnRetry.setOnClickListener(v -> loadBookings());

        loadBookings();
    }

    private void loadBookings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);

        RepositoryCallback<List<BookingDto>> callback = new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setItems(data);
                boolean empty = data == null || data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvError.setText(message);
            }
        };

        if (SessionManager.getInstance(requireContext()).isDriver()) {
            bookingRepository.getBookings(1, null, callback);
        } else {
            bookingRepository.getBookings(1, null, callback);
        }
    }

    @Override
    public void onViewQr(BookingDto booking) {
    }

    @Override
    public void onExtend(BookingDto booking) {
    }

    @Override
    public void onPay(BookingDto booking) {
    }

    @Override
    public void onCancel(BookingDto booking) {
        bookingRepository.cancelBooking(booking.getId(), new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                Toast.makeText(getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                loadBookings();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onChat(BookingDto booking) {
        Intent intent = new Intent(getContext(), com.parkshare.frontend.activities.ChatActivity.class);
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_NAME,
            booking.getParkingSpace() != null ? booking.getParkingSpace().getParkingName() : "Chat");

        if (SessionManager.getInstance(requireContext()).isDriver()) {
            if (booking.getParkingSpace() != null) {
                intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_RECEIVER_ID, booking.getParkingSpace().getOwnerId());
            }
        } else {
            intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_RECEIVER_ID, booking.getUserId());
        }
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.parkshare.frontend.fragments.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.adapters.BookingAdapter;
import com.parkshare.frontend.databinding.FragmentOwnerBookingsBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.LoadingHelper;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.ShimmerUi;

import java.util.List;

public class OwnerBookingsFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private FragmentOwnerBookingsBinding binding;
    private BookingAdapter adapter;
    private ShimmerFrameLayout shimmerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        ShimmerUi.prepareListSkeleton(view, R.layout.shimmer_booking_item, 5);

        adapter = new BookingAdapter(this);
        adapter.setAllowCancel(false);
        adapter.setShowQrButton(false);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(adapter);
        load();
    }

    private void load() {
        LoadingHelper.showShimmer(shimmerLayout, binding.progressBar);
        binding.rvBookings.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.GONE);
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                adapter.setItems(data);
                boolean empty = data == null || data.isEmpty();
                binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvEmpty.setText(message);
            }
        });
    }

    @Override
    public void onCancel(BookingDto booking) {
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
    public void onChat(BookingDto booking) {
        android.content.Intent intent = new android.content.Intent(requireContext(), com.parkshare.frontend.activities.ChatActivity.class);
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_RECEIVER_ID, booking.getUserId());
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(com.parkshare.frontend.activities.ChatActivity.EXTRA_NAME, "Driver #" + booking.getUserId());
        startActivity(intent);
    }

    @Override
    public void onWriteReview(BookingDto booking) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

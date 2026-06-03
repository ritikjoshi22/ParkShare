package com.parkshare.frontend.fragments.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.adapters.BookingAdapter;
import com.parkshare.frontend.databinding.FragmentOwnerBookingsBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class OwnerBookingsFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private FragmentOwnerBookingsBinding binding;
    private BookingAdapter adapter;

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
        adapter = new BookingAdapter(this);
        adapter.setAllowCancel(false);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(adapter);
        load();
    }

    private void load() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setItems(data);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCancel(BookingDto booking) {
        // Owners view only — no cancel from owner side in this MVP
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

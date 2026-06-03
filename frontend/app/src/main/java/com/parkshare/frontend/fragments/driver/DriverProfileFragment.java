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

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.FavoritesActivity;
import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.databinding.FragmentDriverProfileBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class DriverProfileFragment extends Fragment {

    private FragmentDriverProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDriverProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());

        binding.btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FavoritesActivity.class)));
        binding.btnMyBookings.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.driver_bookings));
        binding.btnLogout.setOnClickListener(v -> logout());

        loadStats();
    }

    private void loadStats() {
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                int count = data != null ? data.size() : 0;
                double spent = 0;
                if (data != null) {
                    for (BookingDto b : data) {
                        if ("completed".equals(b.getBookingStatus()) || "confirmed".equals(b.getBookingStatus())) {
                            spent += b.getTotalAmount();
                        }
                    }
                }
                binding.tvBookingsCount.setText(String.valueOf(count));
                binding.tvSpentAmount.setText(String.format(Locale.getDefault(), "NPR %.0f", spent));
            }

            @Override
            public void onError(String message) {
                binding.tvBookingsCount.setText("0");
                binding.tvSpentAmount.setText("NPR 0");
            }
        });
    }

    private void logout() {
        new AuthRepository(sessionManager).logout(new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

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

import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.OwnerStatusDataDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.FavoritesActivity;
import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.activities.owner.OwnerMainActivity;
import com.parkshare.frontend.activities.owner.OwnerVerificationActivity;
import com.parkshare.frontend.activities.owner.OwnerVerificationStatusActivity;
import com.parkshare.frontend.databinding.FragmentProfileBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.repository.OwnerRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());

        if (sessionManager.isDriver()) {
            loadBookingStats();
        }

        binding.btnMyBookings.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigation_bookings));

        binding.btnPayment.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FavoritesActivity.class)));

        binding.btnSwitchMode.setOnClickListener(v -> com.parkshare.frontend.utils.AppModeRouter.handleSwitchToOwner(requireContext()));

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void loadBookingStats() {
        new BookingRepository().getBookings(1, null, new RepositoryCallback<List<BookingDto>>() {
            @Override
            public void onSuccess(List<BookingDto> data) {
                int count = data != null ? data.size() : 0;
                double spent = 0;
                if (data != null) {
                    for (BookingDto b : data) {
                        if ("completed".equals(b.getBookingStatus())
                                || "confirmed".equals(b.getBookingStatus())) {
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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

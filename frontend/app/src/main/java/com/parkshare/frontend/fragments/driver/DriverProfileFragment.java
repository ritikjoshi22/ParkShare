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

import com.bumptech.glide.Glide;
import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.FavoritesActivity;
import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.activities.NotificationsActivity;
import com.parkshare.frontend.databinding.FragmentDriverProfileBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.AppModeRouter;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;
import com.parkshare.frontend.utils.ThemeManager;

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
        
        displayUserInfo();

        binding.btnEditProfile.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show());

        binding.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        binding.btnParkingHistory.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.driver_bookings));

        binding.btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FavoritesActivity.class)));
        
        binding.btnSettings.setOnClickListener(v -> showThemeDialog());

        binding.btnHelpSupport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Help & Support clicked", Toast.LENGTH_SHORT).show());

        binding.btnVerifyOwner.setOnClickListener(v -> AppModeRouter.handleSwitchToOwner(requireContext()));

        binding.cardSwitchToOwner.setOnClickListener(v -> AppModeRouter.handleSwitchToOwner(requireContext()));

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void displayUserInfo() {
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());
        binding.tvUserPhone.setText(sessionManager.getPhone());

        // Load profile image if available
        // Note: You might need to add a method getProfileImage() to SessionManager if it's stored
        // Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_menu_gallery).into(binding.ivLargeProfile);
        // Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_menu_gallery).into(binding.ivSmallProfile);
    }

    private void showThemeDialog() {
        String[] options = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.appearance)
                .setItems(options, (d, which) -> {
                    String mode = ThemeManager.MODE_SYSTEM;
                    if (which == 1) {
                        mode = ThemeManager.MODE_LIGHT;
                    } else if (which == 2) {
                        mode = ThemeManager.MODE_DARK;
                    }
                    ThemeManager.setThemeMode(requireContext(), mode);
                })
                .show();
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

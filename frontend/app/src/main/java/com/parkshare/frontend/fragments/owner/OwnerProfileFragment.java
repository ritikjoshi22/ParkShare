package com.parkshare.frontend.fragments.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.databinding.FragmentOwnerProfileBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.utils.AppModeRouter;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

public class OwnerProfileFragment extends Fragment {

    private FragmentOwnerProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = SessionManager.getInstance(requireContext());
        binding.tvName.setText(session.getFullName());
        binding.tvEmail.setText(session.getEmail());
        binding.tvPhone.setText(session.getPhone());
        binding.cardOwner.setOnClickListener(v -> AppModeRouter.openDriverDashboard(requireContext()));
        binding.btnLogout.setOnClickListener(v ->
                new AuthRepository(session).logout(new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        startActivity(new Intent(requireContext(), LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        requireActivity().finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }
}

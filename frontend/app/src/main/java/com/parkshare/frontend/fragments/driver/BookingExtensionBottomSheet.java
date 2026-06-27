package com.parkshare.frontend.fragments.driver;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.ExtensionOptionsDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.BottomSheetExtensionBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.DateTimeFormatUtil;
import com.parkshare.frontend.utils.RepositoryCallback;

public class BookingExtensionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetExtensionBinding binding;
    private BookingDto booking;
    private ExtensionOptionsDto options;
    private OnExtensionSuccessListener listener;

    public interface OnExtensionSuccessListener {
        void onExtensionSuccess();
    }

    public static BookingExtensionBottomSheet newInstance(BookingDto booking, ExtensionOptionsDto options, OnExtensionSuccessListener listener) {
        BookingExtensionBottomSheet fragment = new BookingExtensionBottomSheet();
        fragment.booking = booking;
        fragment.options = options;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExtensionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOptimizedUI();
    }

    private void setupOptimizedUI() {
        if (options == null || booking == null) {
            dismiss();
            return;
        }

        boolean canExtend = options.canExtend();
        binding.tvStatus.setText(canExtend ? "Extension Available" : "Extension Unavailable");
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), 
            canExtend ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        if (options.getReason() != null) {
            binding.tvReason.setVisibility(View.VISIBLE);
            binding.tvReason.setText(options.getReason());
        }

        if (canExtend) {
            binding.vTimeBar.setVisibility(View.VISIBLE);
            binding.layoutTimeLabels.setVisibility(View.VISIBLE);
            binding.tvCurrentEnd.setText(DateTimeFormatUtil.formatTimeOnly(booking.getEndTime()));
            binding.tvMaxEnd.setText(DateTimeFormatUtil.formatTimeOnly(options.getMaxEndTime()));
            
            setupDynamicChips(options.getMaxMinutes());
        } else {
            binding.chipGroupDuration.setVisibility(View.GONE);
            binding.tvSelectDuration.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
        }

        binding.btnConfirm.setOnClickListener(v -> {
            int checkedId = binding.chipGroupDuration.getCheckedChipId();
            if (checkedId == View.NO_ID) {
                Toast.makeText(getContext(), "Please select duration", Toast.LENGTH_SHORT).show();
                return;
            }

            Chip chip = binding.chipGroupDuration.findViewById(checkedId);
            if (chip.getTag() instanceof Integer) {
                performExtension((Integer) chip.getTag());
            }
        });
    }

    private void setupDynamicChips(int maxMinutes) {
        binding.chipGroupDuration.removeAllViews();
        int[] intervals = {15, 30, 45, 60, 90, 120, 180, 240, 300, 360, 480};
        
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        boolean anyAdded = false;

        for (int minutes : intervals) {
            if (minutes <= maxMinutes) {
                Chip chip = (Chip) inflater.inflate(R.layout.item_duration_chip, binding.chipGroupDuration, false);
                chip.setText(minutes + " min");
                chip.setTag(minutes);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        binding.btnConfirm.setEnabled(true);
                        binding.btnConfirm.setText("Confirm Extension (" + minutes + " min)");
                    }
                });
                binding.chipGroupDuration.addView(chip);
                anyAdded = true;
            }
        }

        if (!anyAdded) {
            binding.tvSelectDuration.setText("No duration blocks available.");
            binding.btnConfirm.setEnabled(false);
        }
    }

    private void performExtension(int minutes) {
        binding.btnConfirm.setEnabled(false);
        binding.btnConfirm.setText("Extending...");

        new BookingRepository().extendBooking(booking.getId(), minutes, new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Booking extended successfully", Toast.LENGTH_LONG).show();
                    if (listener != null) listener.onExtensionSuccess();
                    dismiss();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    binding.btnConfirm.setEnabled(true);
                    binding.btnConfirm.setText("Confirm Extension");
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

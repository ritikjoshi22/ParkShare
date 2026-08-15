package com.parkshare.frontend.activities.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.BookingQuoteDto;
import com.parkshare.api.models.ParkingSlotDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BookingPaymentActivity;
import com.parkshare.frontend.adapters.ParkingSlotVisualAdapter;
import com.parkshare.frontend.databinding.ActivityParkingSlotSelectionBinding;
import com.parkshare.frontend.databinding.BottomSheetBookingSummaryBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.DateTimeFormatUtil;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;
import java.util.Locale;

public class ParkingSlotSelectionActivity extends AppCompatActivity implements ParkingSlotVisualAdapter.Listener {

    public static final String EXTRA_PARKING_ID = "parking_id";
    public static final String EXTRA_PARKING_NAME = "parking_name";
    public static final String EXTRA_PARKING_ADDRESS = "parking_address";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";

    private ActivityParkingSlotSelectionBinding binding;
    private long parkingId;
    private String parkingName;
    private String parkingAddress;
    private String startTimeIso;
    private String endTimeIso;
    
    private ParkingSlotVisualAdapter adapter;
    private ParkingSlotDto selectedSlot;
    private final BookingRepository bookingRepository = new BookingRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParkingSlotSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parkingId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);
        parkingName = getIntent().getStringExtra(EXTRA_PARKING_NAME);
        parkingAddress = getIntent().getStringExtra(EXTRA_PARKING_ADDRESS);
        startTimeIso = getIntent().getStringExtra(EXTRA_START_TIME);
        endTimeIso = getIntent().getStringExtra(EXTRA_END_TIME);

        if (parkingId < 0 || startTimeIso == null || endTimeIso == null) {
            finish();
            return;
        }

        setupHeader();
        setupRecyclerView();
        loadSlots();

        binding.btnConfirm.setText(R.string.proceed_to_summary);
        binding.btnConfirm.setOnClickListener(v -> showSummary());
    }

    private void setupHeader() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ParkingSlotVisualAdapter(this);
        binding.rvSlots.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvSlots.setAdapter(adapter);
    }

    private void loadSlots() {
        binding.tvProgressMessage.setText(R.string.loading);
        binding.progressOverlay.setVisibility(View.VISIBLE);
        bookingRepository.getSlots(parkingId, startTimeIso, endTimeIso, new RepositoryCallback<List<ParkingSlotDto>>() {
            @Override
            public void onSuccess(List<ParkingSlotDto> data) {
                binding.progressOverlay.setVisibility(View.GONE);
                adapter.setItems(data);
            }

            @Override
            public void onError(String message) {
                binding.progressOverlay.setVisibility(View.GONE);
                Toast.makeText(ParkingSlotSelectionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSummary() {
        if (selectedSlot == null) {
            Toast.makeText(this, "Please select a slot first", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvProgressMessage.setText(R.string.loading);
        binding.progressOverlay.setVisibility(View.VISIBLE);
        bookingRepository.quoteBooking(parkingId, selectedSlot.getId(), startTimeIso, endTimeIso, new RepositoryCallback<>() {
            @Override
            public void onSuccess(BookingQuoteDto quote) {
                binding.progressOverlay.setVisibility(View.GONE);
                renderSummaryBottomSheet(quote);
            }

            @Override
            public void onError(String message) {
                binding.progressOverlay.setVisibility(View.GONE);
                Toast.makeText(ParkingSlotSelectionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderSummaryBottomSheet(BookingQuoteDto quote) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetBookingSummaryBinding sheetBinding = BottomSheetBookingSummaryBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvParkingName.setText(parkingName != null ? parkingName : "Parking Space");
        sheetBinding.tvParkingAddress.setText(parkingAddress != null ? parkingAddress : "");
        sheetBinding.tvSelectedSlot.setText(selectedSlot.getLabel());

        long hours = (long) Math.ceil(quote.getHours());
        sheetBinding.tvDuration.setText(String.format(Locale.getDefault(), "%d %s", hours, hours == 1 ? "Hour" : "Hours"));
        sheetBinding.tvDateTimeRange.setText(DateTimeFormatUtil.formatBookingRange(startTimeIso, endTimeIso));

        // Note: Backend provides total with discount applied usually.
        // We match SlotBookingActivity's visual of 10% discount for UI consistency.
        double total = quote.getTotal();
        double basePrice = total / 0.9;
        double discount = basePrice * 0.1;

        sheetBinding.tvBasePrice.setText(String.format(Locale.getDefault(), "NPR %.0f", basePrice));
        sheetBinding.tvDiscount.setText(String.format(Locale.getDefault(), "- NPR %.0f", discount));
        sheetBinding.tvTotalPrice.setText(String.format(Locale.getDefault(), "NPR %.0f", total));

        sheetBinding.btnPayNow.setOnClickListener(v -> {
            dialog.dismiss();
            createFinalBooking();
        });

        dialog.show();
    }

    private void createFinalBooking() {
        binding.tvProgressMessage.setText(R.string.finalizing_booking);
        binding.progressOverlay.setVisibility(View.VISIBLE);

        bookingRepository.createBooking(parkingId, selectedSlot.getId(), startTimeIso, endTimeIso, new RepositoryCallback<>() {
            @Override
            public void onSuccess(BookingDto data) {
                binding.progressOverlay.setVisibility(View.GONE);

                Intent intent = new Intent(ParkingSlotSelectionActivity.this, BookingPaymentActivity.class);
                intent.putExtra(BookingPaymentActivity.EXTRA_BOOKING_ID, data.getId());
                intent.putExtra(BookingPaymentActivity.EXTRA_AMOUNT, data.getAmountDue());
                intent.putExtra(BookingPaymentActivity.EXTRA_TYPE, "booking");
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                binding.progressOverlay.setVisibility(View.GONE);
                Toast.makeText(ParkingSlotSelectionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSlotSelected(ParkingSlotDto slot) {
        if (!"available".equals(slot.getDisplayStatus())) {
            Toast.makeText(this, "This slot is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedSlot = slot;
        adapter.setSelectedSlotId(slot.getId());
    }
}

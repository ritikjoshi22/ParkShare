package com.parkshare.frontend.activities.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.BookingQuoteDto;
import com.parkshare.api.models.ParkingSlotDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BookingPaymentActivity;
import com.parkshare.frontend.activities.BookingQrActivity;
import com.parkshare.frontend.adapters.ParkingSlotAdapter;
import com.parkshare.frontend.databinding.ActivitySlotBookingBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.DateTimeFormatUtil;
import com.parkshare.frontend.utils.LoadingHelper;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.ShimmerUi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SlotBookingActivity extends AppCompatActivity implements ParkingSlotAdapter.Listener {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivitySlotBookingBinding binding;
    private long parkingId;
    private ParkingSlotAdapter adapter;
    private Long selectedSlotId;
    private ShimmerFrameLayout shimmerLayout;

    private LocalDate bookingDate = LocalDate.now();
    private LocalTime startTime = LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
    private int durationHours = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySlotBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parkingId = getIntent().getLongExtra(EXTRA_PARKING_ID, -1);
        if (parkingId < 0) {
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        shimmerLayout = binding.getRoot().findViewById(R.id.shimmerLayout);
        ShimmerUi.prepareListSkeleton(binding.getRoot(), R.layout.shimmer_parking_item, 6);

        adapter = new ParkingSlotAdapter(this);
        binding.rvSlots.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvSlots.setAdapter(adapter);

        binding.btnPickDate.setOnClickListener(v -> pickDate());
        binding.btnPickStartTime.setOnClickListener(v -> pickTime());
        binding.chipDuration.setOnCheckedStateChangeListener((group, checkedIds) -> onDurationChanged());
        binding.btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        updateScheduleLabels();
        refreshAvailability();
    }

    private void onDurationChanged() {
        int checked = binding.chipDuration.getCheckedChipId();
        if (checked == R.id.chip2h) {
            durationHours = 2;
        } else if (checked == R.id.chip3h) {
            durationHours = 3;
        } else if (checked == R.id.chip4h) {
            durationHours = 4;
        } else {
            durationHours = 1;
        }
        refreshAvailability();
    }

    private void pickDate() {
        long selection = bookingDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date)
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(millis -> {
            bookingDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
            if (bookingDate.isBefore(LocalDate.now())) {
                bookingDate = LocalDate.now();
            }
            updateScheduleLabels();
            refreshAvailability();
        });
        picker.show(getSupportFragmentManager(), "booking_date");
    }

    private void pickTime() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(startTime.getHour())
                .setMinute(startTime.getMinute())
                .setTitleText(R.string.start_time)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            startTime = LocalTime.of(picker.getHour(), picker.getMinute());
            updateScheduleLabels();
            refreshAvailability();
        });
        picker.show(getSupportFragmentManager(), "start_time");
    }

    private void updateScheduleLabels() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault());
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault());
        binding.btnPickDate.setText(getString(R.string.pick_date) + ": " + bookingDate.format(dateFmt));
        binding.btnPickStartTime.setText(getString(R.string.arrival_time) + ": " + startTime.format(timeFmt));
    }

    private OffsetDateTime startDateTime() {
        return ZonedDateTime.of(bookingDate, startTime, ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime endDateTime() {
        return startDateTime().plusHours(durationHours);
    }

    private void refreshAvailability() {
        selectedSlotId = null;
        adapter.setSelectedSlotId(null);
        LoadingHelper.showShimmer(shimmerLayout, binding.progressBar);
        binding.rvSlots.setVisibility(View.INVISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
        binding.tvPriceQuote.setVisibility(View.GONE);

        String startIso = DateTimeFormatUtil.toApiIso(startDateTime());
        String endIso = DateTimeFormatUtil.toApiIso(endDateTime());

        new BookingRepository().getSlots(parkingId, startIso, endIso,
                new RepositoryCallback<List<ParkingSlotDto>>() {
                    @Override
                    public void onSuccess(List<ParkingSlotDto> data) {
                        LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                        binding.rvSlots.setVisibility(View.VISIBLE);
                        adapter.setItems(data);
                        boolean empty = data == null || data.isEmpty();
                        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                        fetchQuote();
                    }

                    @Override
                    public void onError(String message) {
                        LoadingHelper.hideAll(shimmerLayout, binding.progressBar);
                        binding.rvSlots.setVisibility(View.VISIBLE);
                        Toast.makeText(SlotBookingActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchQuote() {
        String startIso = DateTimeFormatUtil.toApiIso(startDateTime());
        String endIso = DateTimeFormatUtil.toApiIso(endDateTime());
        new BookingRepository().quoteBooking(parkingId, selectedSlotId, startIso, endIso,
                new RepositoryCallback<BookingQuoteDto>() {
                    @Override
                    public void onSuccess(BookingQuoteDto data) {
                        if (data != null) {
                            binding.tvPriceQuote.setVisibility(View.VISIBLE);
                            String cap = data.isDailyCapApplied() ? " (daily cap applied)" : "";
                            binding.tvPriceQuote.setText(String.format(Locale.getDefault(),
                                    "Estimated total: NPR %.0f · %.1f hr%s", data.getTotal(), data.getHours(), cap));
                        }
                    }

                    @Override
                    public void onError(String message) {
                        binding.tvPriceQuote.setVisibility(View.GONE);
                    }
                });
    }

    private void confirmBooking() {
        if (selectedSlotId == null) {
            Toast.makeText(this, R.string.select_slot_first, Toast.LENGTH_SHORT).show();
            return;
        }

        String startIso = DateTimeFormatUtil.toApiIso(startDateTime());
        String endIso = DateTimeFormatUtil.toApiIso(endDateTime());

        binding.btnConfirmBooking.setEnabled(false);
        new BookingRepository().createBooking(parkingId, selectedSlotId, startIso, endIso,
                new RepositoryCallback<BookingDto>() {
                    @Override
                    public void onSuccess(BookingDto data) {
                        binding.btnConfirmBooking.setEnabled(true);
                        if (data == null) {
                            return;
                        }
                        Toast.makeText(SlotBookingActivity.this, R.string.booking_success, Toast.LENGTH_LONG).show();
                        
                        if (data.getAmountDue() > 0 && !"paid".equals(data.getPaymentStatus())) {
                            // Redirect to Payment first
                            Intent pay = new Intent(SlotBookingActivity.this, BookingPaymentActivity.class);
                            pay.putExtra(BookingPaymentActivity.EXTRA_BOOKING_ID, data.getId());
                            pay.putExtra(BookingPaymentActivity.EXTRA_AMOUNT, data.getAmountDue());
                            pay.putExtra(BookingPaymentActivity.EXTRA_TYPE, "booking");
                            startActivity(pay);
                        } else {
                            // Already paid (unlikely for new booking but handle it)
                            startActivity(BookingQrActivity.intent(SlotBookingActivity.this, data));
                        }
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        binding.btnConfirmBooking.setEnabled(true);
                        Toast.makeText(SlotBookingActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onSlotSelected(ParkingSlotDto slot) {
        if (!"available".equals(slot.getDisplayStatus())) {
            Toast.makeText(this, R.string.slot_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        selectedSlotId = slot.getId();
        adapter.setSelectedSlotId(selectedSlotId);
        fetchQuote();
    }
}

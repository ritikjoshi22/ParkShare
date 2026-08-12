package com.parkshare.frontend.activities.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.BookingQuoteDto;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.BookingPaymentActivity;
import com.parkshare.frontend.activities.BookingQrActivity;
import com.parkshare.frontend.adapters.BookingDateAdapter;
import com.parkshare.frontend.databinding.ActivitySlotBookingBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.repository.ParkingRepository;
import com.parkshare.frontend.utils.DateTimeFormatUtil;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlotBookingActivity extends AppCompatActivity {

    public static final String EXTRA_PARKING_ID = "parking_id";

    private ActivitySlotBookingBinding binding;
    private long parkingId;
    private BookingDateAdapter dateAdapter;
    private final BookingRepository bookingRepository = new BookingRepository();
    private final ParkingRepository parkingRepository = new ParkingRepository();

    private LocalDate bookingDate = LocalDate.now();
    private LocalTime startTime = LocalTime.of(10, 0);
    private int durationHours = 3;

    private String parkingName;
    private String parkingAddress;
    private double hourlyRate;

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

        setupHeader();
        loadParkingSummary();
        setupDateList();
        setupTimeSpinners();
        
        binding.btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void setupHeader() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadParkingSummary() {
        parkingRepository.getById(parkingId, new RepositoryCallback<ParkingSpaceDto>() {
            @Override
            public void onSuccess(ParkingSpaceDto data) {
                parkingName = data.getParkingName();
                parkingAddress = data.getAddress();
                hourlyRate = data.getPricePerHour();

                binding.tvParkingName.setText(parkingName);
                binding.tvParkingAddress.setText(parkingAddress);
                binding.tvParkingPrice.setText(String.format(Locale.getDefault(), "Rs. %.0f /hr", hourlyRate));
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void setupDateList() {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dates.add(LocalDate.now().plusDays(i));
        }
        dateAdapter = new BookingDateAdapter(dates, date -> {
            bookingDate = date;
            fetchQuote();
        });
        binding.rvBookingDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvBookingDates.setAdapter(dateAdapter);
    }

    private void setupTimeSpinners() {
        List<String> times = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            times.add(String.format(Locale.getDefault(), "%02d:00 %s", (h == 0 || h == 12) ? 12 : h % 12, h < 12 ? "AM" : "PM"));
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, times);
        binding.spinnerStartTime.setAdapter(timeAdapter);
        binding.spinnerStartTime.setOnItemClickListener((parent, view, position, id) -> {
            startTime = LocalTime.of(position, 0);
            fetchQuote();
        });

        String[] durations = {"1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours"};
        ArrayAdapter<String> durAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, durations);
        binding.spinnerDuration.setAdapter(durAdapter);
        binding.spinnerDuration.setOnItemClickListener((parent, view, position, id) -> {
            durationHours = position + 1;
            fetchQuote();
        });
        
        fetchQuote();
    }

    private void fetchQuote() {
        String startIso = DateTimeFormatUtil.toApiIso(ZonedDateTime.of(bookingDate, startTime, ZoneId.systemDefault()).toOffsetDateTime());
        String endIso = DateTimeFormatUtil.toApiIso(ZonedDateTime.of(bookingDate, startTime.plusHours(durationHours), ZoneId.systemDefault()).toOffsetDateTime());
        
        bookingRepository.quoteBooking(parkingId, null, startIso, endIso, new RepositoryCallback<BookingQuoteDto>() {
            @Override
            public void onSuccess(BookingQuoteDto data) {
                binding.tvTotalPrice.setText("Rs. " + (int)data.getTotal());
                binding.tvOriginalPrice.setText("Rs. " + (int)(data.getTotal() / 0.9));
                binding.tvDiscountLabel.setText("(10% discount)");
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void confirmBooking() {
        String startIso = DateTimeFormatUtil.toApiIso(ZonedDateTime.of(bookingDate, startTime, ZoneId.systemDefault()).toOffsetDateTime());
        String endIso = DateTimeFormatUtil.toApiIso(ZonedDateTime.of(bookingDate, startTime.plusHours(durationHours), ZoneId.systemDefault()).toOffsetDateTime());

        Intent intent = new Intent(this, ParkingSlotSelectionActivity.class);
        intent.putExtra(ParkingSlotSelectionActivity.EXTRA_PARKING_ID, parkingId);
        intent.putExtra(ParkingSlotSelectionActivity.EXTRA_PARKING_NAME, parkingName);
        intent.putExtra(ParkingSlotSelectionActivity.EXTRA_PARKING_ADDRESS, parkingAddress);
        intent.putExtra(ParkingSlotSelectionActivity.EXTRA_START_TIME, startIso);
        intent.putExtra(ParkingSlotSelectionActivity.EXTRA_END_TIME, endIso);
        startActivity(intent);
    }
}

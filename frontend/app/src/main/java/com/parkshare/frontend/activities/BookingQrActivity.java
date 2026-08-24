package com.parkshare.frontend.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityBookingQrBinding;
import com.parkshare.frontend.activities.driver.DriverMainActivity;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.DateTimeFormatUtil;
import com.parkshare.frontend.utils.QrCodeHelper;
import com.parkshare.frontend.utils.RepositoryCallback;

/**
 * Driver displays booking QR for the owner to scan. Owners do not use this screen.
 */
public class BookingQrActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_QR_CODE = "qr_code";
    public static final String EXTRA_PARKING_NAME = "parking_name";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";
    public static final String EXTRA_SLOT_LABEL = "slot_label";
    public static final String EXTRA_DURATION = "duration";

    private ActivityBookingQrBinding binding;
    private long bookingId;

    public static Intent intent(Context context, BookingDto booking) {
        Intent intent = new Intent(context, BookingQrActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(EXTRA_QR_CODE, booking.getQrCode());
        if (booking.getParkingSpace() != null) {
            intent.putExtra(EXTRA_PARKING_NAME, booking.getParkingSpace().getParkingName());
        }
        if (booking.getParkingSlot() != null) {
            intent.putExtra(EXTRA_SLOT_LABEL, booking.getParkingSlot().getLabel());
        }
        intent.putExtra(EXTRA_START_TIME, booking.getStartTime());
        intent.putExtra(EXTRA_END_TIME, booking.getEndTime());

        long hours = (long) Math.ceil(booking.getTotalAmount() > 0 ? (booking.getTotalHours() > 0 ? booking.getTotalHours() : 3) : 3);
        intent.putExtra(EXTRA_DURATION, hours + (hours == 1 ? " Hour" : " Hours"));

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        String qrCode = getIntent().getStringExtra(EXTRA_QR_CODE);

        String parkingName = getIntent().getStringExtra(EXTRA_PARKING_NAME);
        binding.tvParkingName.setText(parkingName != null && !parkingName.isEmpty()
                ? parkingName : "Parking Space");

        String slotLabel = getIntent().getStringExtra(EXTRA_SLOT_LABEL);
        binding.tvSlotLabel.setText(slotLabel != null ? slotLabel : "—");

        String duration = getIntent().getStringExtra(EXTRA_DURATION);
        binding.tvDurationValue.setText(duration != null ? duration : "—");

        String start = getIntent().getStringExtra(EXTRA_START_TIME);
        String end = getIntent().getStringExtra(EXTRA_END_TIME);
        binding.tvBookingDate.setText(DateTimeFormatUtil.formatBookingDate(start));

        binding.tvReference.setText("Reference: PS-" + bookingId + "-B");

        animateEntrance();

        binding.btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, DriverMainActivity.class);
            intent.putExtra(DriverMainActivity.EXTRA_TARGET_TAB, R.id.driver_bookings);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.btnReview.setOnClickListener(v -> {
            Intent reviewIntent = new Intent(this, ParkingReviewActivity.class);
            reviewIntent.putExtra(ParkingReviewActivity.EXTRA_PARKING_ID, 0L); // Need real ID if available, otherwise just open
            reviewIntent.putExtra(ParkingReviewActivity.EXTRA_PARKING_NAME, parkingName);
            startActivity(reviewIntent);
        });

        if (qrCode != null && !qrCode.isEmpty()) {
            renderQr(qrCode);
        } else if (bookingId > 0) {
            refreshFromServer();
        } else {
            Toast.makeText(this, R.string.qr_not_available, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void animateEntrance() {
        binding.ivSuccessIcon.setAlpha(0f);
        binding.ivSuccessIcon.setScaleX(0.5f);
        binding.ivSuccessIcon.setScaleY(0.5f);
        binding.ivSuccessIcon.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(200)
                .start();

        binding.ticketCard.setTranslationY(100f);
        binding.ticketCard.setAlpha(0f);
        binding.ticketCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .start();

        binding.buttonContainer.setAlpha(0f);
        binding.buttonContainer.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(800)
                .start();
    }

    private void refreshFromServer() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new BookingRepository().getBooking(bookingId, new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                binding.progressBar.setVisibility(View.GONE);
                if (data == null || data.getQrCode() == null || data.getQrCode().isEmpty()) {
                    Toast.makeText(BookingQrActivity.this, R.string.qr_not_available, Toast.LENGTH_LONG).show();
                    return;
                }
                if (data.getParkingSpace() != null) {
                    binding.tvParkingName.setText(data.getParkingSpace().getParkingName());
                }
                if (data.getParkingSlot() != null) {
                    binding.tvSlotLabel.setText(data.getParkingSlot().getLabel());
                }
                binding.tvBookingDate.setText(DateTimeFormatUtil.formatBookingDate(data.getStartTime()));
                long hours = (long) Math.ceil(data.getTotalHours() > 0 ? data.getTotalHours() : 3);
                binding.tvDurationValue.setText(hours + (hours == 1 ? " Hour" : " Hours"));

                renderQr(data.getQrCode());
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingQrActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderQr(String code) {
        int size = (int) (280 * getResources().getDisplayMetrics().density);
        android.graphics.Bitmap bitmap = QrCodeHelper.generate(code, size);
        if (bitmap != null) {
            binding.ivQr.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, R.string.qr_not_available, Toast.LENGTH_SHORT).show();
        }
    }
}

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

    private ActivityBookingQrBinding binding;
    private long bookingId;

    public static Intent intent(Context context, BookingDto booking) {
        Intent intent = new Intent(context, BookingQrActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(EXTRA_QR_CODE, booking.getQrCode());
        if (booking.getParkingSpace() != null) {
            intent.putExtra(EXTRA_PARKING_NAME, booking.getParkingSpace().getParkingName());
        }
        intent.putExtra(EXTRA_START_TIME, booking.getStartTime());
        intent.putExtra(EXTRA_END_TIME, booking.getEndTime());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnRegenerate.setVisibility(View.GONE);

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        String qrCode = getIntent().getStringExtra(EXTRA_QR_CODE);

        String parkingName = getIntent().getStringExtra(EXTRA_PARKING_NAME);
        binding.tvParkingName.setText(parkingName != null && !parkingName.isEmpty()
                ? parkingName : getString(R.string.booking_qr_title));

        String start = getIntent().getStringExtra(EXTRA_START_TIME);
        String end = getIntent().getStringExtra(EXTRA_END_TIME);
        binding.tvBookingTime.setText(DateTimeFormatUtil.formatBookingRange(start, end));

        if (qrCode != null && !qrCode.isEmpty()) {
            renderQr(qrCode);
        } else if (bookingId > 0) {
            refreshFromServer();
        } else {
            Toast.makeText(this, R.string.qr_not_available, Toast.LENGTH_LONG).show();
            finish();
        }
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
                binding.tvBookingTime.setText(
                        DateTimeFormatUtil.formatBookingRange(data.getStartTime(), data.getEndTime()));
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

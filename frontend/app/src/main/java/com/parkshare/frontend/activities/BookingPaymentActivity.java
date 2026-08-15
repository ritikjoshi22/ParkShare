package com.parkshare.frontend.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.PaymentIntentDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.databinding.ActivityBookingPaymentBinding;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.Locale;

public class BookingPaymentActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_TYPE = "payment_type";

    private ActivityBookingPaymentBinding binding;
    private long bookingId;
    private PaymentSheet paymentSheet;
    private String paymentIntentId;
    private CountDownTimer countDownTimer;
    private long expiryTimeMillis;
    private static final long RESERVATION_TIMEOUT = 15 * 60 * 1000; // 15 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String intentType = getIntent().getStringExtra(EXTRA_TYPE);
        String type = intentType != null ? intentType : "booking";

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvAmount.setText(String.format(Locale.getDefault(), "NPR %.2f", amount));
        binding.btnPay.setOnClickListener(v -> startPayment(type));

        if (savedInstanceState != null) {
            expiryTimeMillis = savedInstanceState.getLong("expiry_time");
        } else {
            expiryTimeMillis = System.currentTimeMillis() + RESERVATION_TIMEOUT;
        }

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        startReservationTimer(type);
    }

    private void startReservationTimer(String type) {
        if (!"booking".equals(type)) return;

        long remaining = expiryTimeMillis - System.currentTimeMillis();
        if (remaining <= 0) {
            handleReservationExpired();
            return;
        }

        binding.tvTimer.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                binding.tvTimer.setText(getString(R.string.reservation_expires_in, timeFormatted));
            }

            @Override
            public void onFinish() {
                handleReservationExpired();
            }
        }.start();
    }

    private void handleReservationExpired() {
        binding.tvTimer.setText(R.string.reservation_expired);
        binding.btnPay.setEnabled(false);
        Toast.makeText(BookingPaymentActivity.this, R.string.reservation_expired, Toast.LENGTH_LONG).show();
        new android.os.Handler().postDelayed(this::finish, 3000);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("expiry_time", expiryTimeMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startPayment(String type) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPay.setEnabled(false);
        new BookingRepository().createPaymentIntent(bookingId, type, new RepositoryCallback<PaymentIntentDto>() {
            @Override
            public void onSuccess(PaymentIntentDto data) {
                if (data == null) {
                    onPaymentError("Payment failed");
                    return;
                }
                if (data.isDevMode() || data.getClientSecret() == null || data.getClientSecret().isEmpty()) {
                    confirmDevPayment(data.getPaymentIntentId());
                } else {
                    paymentIntentId = data.getPaymentIntentId();
                    PaymentConfiguration.init(getApplicationContext(), data.getPublishableKey());
                    presentPaymentSheet(data.getClientSecret());
                }
            }

            @Override
            public void onError(String message) {
                if (message != null && message.contains("No payment required")) {
                    // Booking might already be paid/confirmed, go straight to QR
                    redirectToQr();
                } else {
                    onPaymentError(message);
                }
            }
        });
    }

    private void redirectToQr() {
        new BookingRepository().getBooking(bookingId, new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                startActivity(BookingQrActivity.intent(BookingPaymentActivity.this, data));
                finish();
            }

            @Override
            public void onError(String message) {
                finish();
            }
        });
    }

    private void confirmDevPayment(String intentId) {
        new BookingRepository().confirmPayment(bookingId, intentId, new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
                if (countDownTimer != null) countDownTimer.cancel();
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingPaymentActivity.this, R.string.payment_success, Toast.LENGTH_LONG).show();
                
                // Redirect to QR activity after successful payment
                new BookingRepository().getBooking(bookingId, new RepositoryCallback<BookingDto>() {
                    @Override
                    public void onSuccess(BookingDto data) {
                        startActivity(BookingQrActivity.intent(BookingPaymentActivity.this, data));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        finish();
                    }
                });
            }

            @Override
            public void onError(String message) {
                onPaymentError(message);
            }
        });
    }

    private void onPaymentError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnPay.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void presentPaymentSheet(String clientSecret) {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("ParkShare")
                .build();
        paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
        binding.progressBar.setVisibility(View.GONE);
        binding.btnPay.setEnabled(true);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            binding.progressBar.setVisibility(View.VISIBLE);
            confirmDevPayment(paymentIntentId); // Reusing the confirm logic
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            onPaymentError("Payment canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            onPaymentError(((PaymentSheetResult.Failed) paymentSheetResult).getError().getLocalizedMessage());
        }
    }
}

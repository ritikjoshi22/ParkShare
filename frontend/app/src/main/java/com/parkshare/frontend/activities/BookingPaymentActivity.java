package com.parkshare.frontend.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
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
                onPaymentError(message);
            }
        });
    }

    private void confirmDevPayment(String intentId) {
        new BookingRepository().confirmPayment(bookingId, intentId, new RepositoryCallback<BookingDto>() {
            @Override
            public void onSuccess(BookingDto data) {
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

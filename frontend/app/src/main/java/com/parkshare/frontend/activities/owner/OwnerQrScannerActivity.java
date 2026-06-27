package com.parkshare.frontend.activities.owner;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.parkshare.api.models.BookingScanResultDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.repository.BookingRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.RoleRouter;

public class OwnerQrScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private View scanLine;
    private View layoutSuccess;
    private View layoutError;
    private ProgressBar progressBar;
    private boolean torchOn;
    private boolean processing;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleRouter.isRoleAllowed(this, "owner")) {
            finish();
            return;
        }
        setContentView(R.layout.activity_owner_qr_scanner);

        barcodeView = findViewById(R.id.barcodeView);
        barcodeView.setStatusText("");
        View statusView = barcodeView.getStatusView();
        if (statusView != null) {
            statusView.setVisibility(View.GONE);
        }
        scanLine = findViewById(R.id.scanLine);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        layoutError = findViewById(R.id.layoutError);
        progressBar = findViewById(R.id.progressBar);

        ImageButton btnClose = findViewById(R.id.btnClose);
        ImageButton btnFlash = findViewById(R.id.btnFlash);
        btnClose.setOnClickListener(v -> finish());
        btnFlash.setOnClickListener(v -> toggleFlash(btnFlash));

        Animation lineAnim = AnimationUtils.loadAnimation(this, R.anim.scan_line_pulse);
        scanLine.startAnimation(lineAnim);

        setupResultScreens();
        ensureCameraPermission();
    }

    private void setupResultScreens() {
        MaterialButton btnScanAgain = layoutSuccess.findViewById(R.id.btnScanAgain);
        MaterialButton btnDone = layoutSuccess.findViewById(R.id.btnDone);
        MaterialButton btnTryAgain = layoutError.findViewById(R.id.btnTryAgain);
        btnScanAgain.setOnClickListener(v -> resumeScanning());
        btnDone.setOnClickListener(v -> finish());
        btnTryAgain.setOnClickListener(v -> resumeScanning());
    }

    private void ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (processing || result.getText() == null) {
                    return;
                }
                processing = true;
                barcodeView.pause();
                submitScan(result.getText());
            }
        });
        barcodeView.resume();
    }

    private void toggleFlash(ImageButton btnFlash) {
        torchOn = !torchOn;
        if (torchOn) {
            barcodeView.setTorchOn();
            btnFlash.setImageResource(R.drawable.ic_flash_on);
        } else {
            barcodeView.setTorchOff();
            btnFlash.setImageResource(R.drawable.ic_flash_off);
        }
    }

    private void submitScan(String payload) {
        progressBar.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.GONE);

        new BookingRepository().scanBookingQr(payload.trim(), new RepositoryCallback<BookingScanResultDto>() {
            @Override
            public void onSuccess(BookingScanResultDto data) {
                progressBar.setVisibility(View.GONE);
                if (data == null) {
                    showError(getString(R.string.scan_failed));
                    return;
                }
                showSuccess(data);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void showSuccess(@NonNull BookingScanResultDto data) {
        if ("payment_required".equals(data.getAction()) || data.isPaymentRequired()) {
            showError(getString(R.string.payment_pending_scanner,
                    String.format(java.util.Locale.getDefault(), "NPR %.2f", data.getAmountDue())));
            return;
        }
        layoutSuccess.setVisibility(View.VISIBLE);
        TextView title = layoutSuccess.findViewById(R.id.tvSuccessTitle);
        TextView message = layoutSuccess.findViewById(R.id.tvSuccessMessage);
        if ("check_out".equals(data.getAction())) {
            title.setText(R.string.check_out_success);
        } else {
            title.setText(R.string.check_in_success);
        }
        message.setText(data.getMessage() != null ? data.getMessage() : getString(R.string.scan_success));
        ObjectAnimator.ofFloat(layoutSuccess, View.ALPHA, 0f, 1f).setDuration(250).start();
    }

    private void showError(String message) {
        layoutError.setVisibility(View.VISIBLE);
        TextView tv = layoutError.findViewById(R.id.tvErrorMessage);
        tv.setText(message);
        ObjectAnimator.ofFloat(layoutError, View.ALPHA, 0f, 1f).setDuration(250).start();
    }

    private void resumeScanning() {
        processing = false;
        layoutSuccess.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        barcodeView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        barcodeView.pause();
        super.onPause();
    }
}

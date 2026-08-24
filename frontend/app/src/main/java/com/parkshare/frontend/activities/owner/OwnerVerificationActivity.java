package com.parkshare.frontend.activities.owner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.parkshare.api.models.OwnerDocumentDto;
import com.parkshare.api.models.OwnerProfileDto;
import com.parkshare.api.models.OwnerStatusDataDto;
import com.parkshare.frontend.R;
import com.parkshare.frontend.activities.owner.LocationPickerActivity;
import com.parkshare.frontend.repository.OwnerRepository;
import com.parkshare.frontend.utils.RepositoryCallback;
import com.parkshare.frontend.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class OwnerVerificationActivity extends AppCompatActivity {

    private static final String EXTRA_STEP = "step";
    private static final String EXTRA_REJECTION = "rejection_reason";

    private FrameLayout stepContainer;
    private OwnerRepository ownerRepository;
    private SessionManager sessionManager;
    private int currentStep = 1;
    private String pendingDocumentType;
    private OwnerProfileDto profile;

    private boolean featureCovered;
    private boolean featureCctv;
    private boolean featureSecurity;
    private boolean featureEv;

    private final Set<String> uploadedDocs = new HashSet<>();

    private final ActivityResultLauncher<String[]> documentPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null && pendingDocumentType != null) {
                    uploadDocument(uri, pendingDocumentType);
                }
            });

    private final ActivityResultLauncher<Intent> locationPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && profile != null) {
                    Intent data = result.getData();
                    Map<String, Object> body = new HashMap<>();
                    body.put("latitude", data.getDoubleExtra(LocationPickerActivity.EXTRA_LAT, 0));
                    body.put("longitude", data.getDoubleExtra(LocationPickerActivity.EXTRA_LNG, 0));
                    String address = data.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS);
                    if (address != null) {
                        body.put("address", address);
                    }
                    saveStep(2, body, false);
                }
            });

    public static Intent intent(Context context, int step, @Nullable String rejectionReason) {
        Intent intent = new Intent(context, OwnerVerificationActivity.class);
        intent.putExtra(EXTRA_STEP, step);
        if (rejectionReason != null) {
            intent.putExtra(EXTRA_REJECTION, rejectionReason);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_verification);
        stepContainer = findViewById(R.id.stepContainer);
        ownerRepository = new OwnerRepository();
        sessionManager = SessionManager.getInstance(this);
        currentStep = Math.max(1, Math.min(4, getIntent().getIntExtra(EXTRA_STEP, 1)));

        String rejection = getIntent().getStringExtra(EXTRA_REJECTION);
        if (rejection != null && !rejection.isEmpty()) {
            Toast.makeText(this, "Rejected: " + rejection, Toast.LENGTH_LONG).show();
        }

        loadProfile();
    }

    private void loadProfile() {
        ownerRepository.fetchVerification(new RepositoryCallback<OwnerProfileDto>() {
            @Override
            public void onSuccess(OwnerProfileDto data) {
                profile = data;
                refreshUploadedDocs(data);
                applyStepData(data);
                if (currentStep <= 0) {
                    currentStep = data.getCurrentStep();
                }
                showStep(currentStep);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OwnerVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                showStep(currentStep);
            }
        });
    }

    private void refreshUploadedDocs(OwnerProfileDto data) {
        uploadedDocs.clear();
        List<OwnerDocumentDto> docs = data.getDocuments();
        if (docs != null) {
            for (OwnerDocumentDto doc : docs) {
                uploadedDocs.add(doc.getDocumentType());
            }
        }
        updateUploadViews();
    }

    private void updateUploadViews() {
        int[] uploadIds = {
                R.id.btnUploadFront, R.id.btnUploadBack, R.id.btnUploadLicense, R.id.ivSelfie,
                R.id.btnUploadLalpurja, R.id.btnUploadTaxReceipt,
                R.id.cardEntryGate, R.id.cardFrontView, R.id.cardBackView, R.id.cardCctv
        };
        String[] docTypes = {
                "pan_front", "pan_back", "license", "selfie",
                "lalpurja", "tax_receipt",
                "entry_gate", "front_view", "back_view", "cctv"
        };

        for (int i = 0; i < uploadIds.length; i++) {
            View v = findViewById(uploadIds[i]);
            if (v != null) {
                boolean uploaded = uploadedDocs.contains(docTypes[i]);
                v.setAlpha(uploaded ? 0.5f : 1.0f);
                if (v instanceof MaterialCardView) {
                    ((MaterialCardView) v).setStrokeColor(uploaded ? getColor(R.color.secondary) : getColor(R.color.gray_300));
                }
            }
        }
    }

    private void applyStepData(OwnerProfileDto data) {
        Map<String, Map<String, Object>> stepData = data.getStepData();
        if (stepData == null) {
            return;
        }
        Map<String, Object> step4 = stepData.get("step_4");
        if (step4 != null) {
            featureCovered = bool(step4.get("feature_covered"));
            featureCctv = bool(step4.get("feature_cctv"));
            featureSecurity = bool(step4.get("feature_security"));
            featureEv = bool(step4.get("feature_ev"));
        }
    }

    private boolean bool(Object value) {
        return value instanceof Boolean && (Boolean) value;
    }

    private void showStep(int step) {
        currentStep = step;
        int layoutRes;
        switch (step) {
            case 2:
                layoutRes = R.layout.owner_verification2;
                break;
            case 3:
                layoutRes = R.layout.owner_verification3;
                break;
            case 4:
                layoutRes = R.layout.owner_verification4;
                break;
            default:
                layoutRes = R.layout.owner_verification;
                break;
        }
        stepContainer.removeAllViews();
        View root = LayoutInflater.from(this).inflate(layoutRes, stepContainer, true);
        wireStep(root, step);
        updateUploadViews();
    }

    private void wireStep(View root, int step) {
        switch (step) {
            case 1:
                wireStep1(root);
                break;
            case 2:
                wireStep2(root);
                break;
            case 3:
                wireStep3(root);
                break;
            case 4:
                wireStep4(root);
                break;
            default:
                break;
        }
    }

    private void wireStep1(View root) {
        View close = root.findViewById(R.id.btnClose);
        if (close != null) {
            close.setOnClickListener(v -> finish());
        }
        TextInputEditText pan = root.findViewById(R.id.panNumber);
        CheckBox consent = root.findViewById(R.id.consent);

        if (profile != null && profile.getStepData() != null) {
            Map<String, Object> step1 = profile.getStepData().get("step_1");
            if (step1 != null && pan != null && step1.get("pan_number") != null) {
                pan.setText(String.valueOf(step1.get("pan_number")));
            }
            if (step1 != null && consent != null && step1.get("consent") instanceof Boolean) {
                consent.setChecked((Boolean) step1.get("consent"));
            }
        }

        bindUpload(root, R.id.btnUploadFront, "pan_front");
        bindUpload(root, R.id.btnUploadBack, "pan_back");
        bindUpload(root, R.id.btnUploadLicense, "license");
        bindUpload(root, R.id.ivSelfie, "selfie");
        bindUpload(root, R.id.btnRetake, "selfie");

        MaterialButton save = root.findViewById(R.id.btnSaveDraft);
        MaterialButton cont = root.findViewById(R.id.btnContinue);
        if (save != null) {
            save.setOnClickListener(v -> saveStep1(pan, consent, false));
        }
        if (cont != null) {
            cont.setOnClickListener(v -> saveStep1(pan, consent, true));
        }
    }

    private void wireStep2(View root) {
        root.findViewById(R.id.btnBack).setOnClickListener(v -> showStep(1));
        bindUpload(root, R.id.btnUploadLalpurja, "lalpurja");
        bindUpload(root, R.id.btnUploadTaxReceipt, "tax_receipt");
        root.findViewById(R.id.btnCurrentLocation).setOnClickListener(v -> useCurrentLocation());
        MaterialButton save = root.findViewById(R.id.btnSaveDraft);
        MaterialButton cont = root.findViewById(R.id.btnContinue);
        if (save != null) {
            save.setOnClickListener(v -> saveStep(2, new HashMap<>(), false));
        }
        if (cont != null) {
            cont.setOnClickListener(v -> saveStep(2, new HashMap<>(), true));
        }
    }

    private void wireStep3(View root) {
        root.findViewById(R.id.btnBack).setOnClickListener(v -> showStep(2));
        bindUpload(root, R.id.cardEntryGate, "entry_gate");
        bindUpload(root, R.id.cardFrontView, "front_view");
        bindUpload(root, R.id.cardBackView, "back_view");
        bindUpload(root, R.id.cardCctv, "cctv");
        MaterialButton save = root.findViewById(R.id.btnSaveDraft);
        MaterialButton cont = root.findViewById(R.id.btnContinue);
        if (save != null) {
            save.setOnClickListener(v -> saveStep(3, new HashMap<>(), false));
        }
        if (cont != null) {
            cont.setOnClickListener(v -> saveStep(3, new HashMap<>(), true));
        }
    }

    private void wireStep4(View root) {
        root.findViewById(R.id.btnBack).setOnClickListener(v -> showStep(3));
        MaterialCardView cardCovered = root.findViewById(R.id.cardCovered);
        MaterialCardView cardCctv = root.findViewById(R.id.cardCCTV);
        MaterialCardView cardSecurity = root.findViewById(R.id.cardSecurity);
        MaterialCardView cardEv = root.findViewById(R.id.cardEV);
        toggleCard(cardCovered, featureCovered);
        toggleCard(cardCctv, featureCctv);
        toggleCard(cardSecurity, featureSecurity);
        toggleCard(cardEv, featureEv);
        cardCovered.setOnClickListener(v -> {
            featureCovered = !featureCovered;
            toggleCard(cardCovered, featureCovered);
        });
        cardCctv.setOnClickListener(v -> {
            featureCctv = !featureCctv;
            toggleCard(cardCctv, featureCctv);
        });
        cardSecurity.setOnClickListener(v -> {
            featureSecurity = !featureSecurity;
            toggleCard(cardSecurity, featureSecurity);
        });
        cardEv.setOnClickListener(v -> {
            featureEv = !featureEv;
            toggleCard(cardEv, featureEv);
        });
        MaterialButton save = root.findViewById(R.id.btnSaveDraft);
        MaterialButton submit = root.findViewById(R.id.btnVerifyPublish);
        if (save != null) {
            save.setOnClickListener(v -> saveStep4(false));
        }
        if (submit != null) {
            submit.setOnClickListener(v -> saveStep4(true));
        }
    }

    private void toggleCard(MaterialCardView card, boolean selected) {
        int stroke = selected ? getColor(R.color.primary) : getColor(android.R.color.darker_gray);
        card.setStrokeColor(stroke);
        card.setStrokeWidth(selected ? 4 : 1);
    }

    private void saveStep1(TextInputEditText pan, CheckBox consent, boolean advance) {
        Map<String, Object> body = new HashMap<>();
        if (pan != null && pan.getText() != null) {
            body.put("pan_number", pan.getText().toString().trim().toUpperCase());
        }
        body.put("consent", consent != null && consent.isChecked());
        saveStep(1, body, advance);
    }

    private void saveStep(int step, Map<String, Object> body, boolean advance) {
        ownerRepository.saveStep(step, body, new RepositoryCallback<OwnerProfileDto>() {
            @Override
            public void onSuccess(OwnerProfileDto data) {
                profile = data;
                refreshOwnerStatus(advance ? Math.min(4, step + 1) : currentStep);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OwnerVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshOwnerStatus(int nextStep) {
        ownerRepository.fetchStatus(new RepositoryCallback<OwnerStatusDataDto>() {
            @Override
            public void onSuccess(OwnerStatusDataDto data) {
                sessionManager.updateOwnerCache(data);
                Toast.makeText(OwnerVerificationActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                if (nextStep != currentStep) {
                    showStep(nextStep);
                }
            }

            @Override
            public void onError(String message) {
                if (nextStep != currentStep) {
                    showStep(nextStep);
                }
            }
        });
    }

    private void saveStep4(boolean submitAfter) {
        Map<String, Object> body = new HashMap<>();
        body.put("feature_covered", featureCovered);
        body.put("feature_cctv", featureCctv);
        body.put("feature_security", featureSecurity);
        body.put("feature_ev", featureEv);
        ownerRepository.saveStep(4, body, new RepositoryCallback<OwnerProfileDto>() {
            @Override
            public void onSuccess(OwnerProfileDto data) {
                profile = data;
                if (submitAfter) {
                    submitVerification();
                } else {
                    refreshOwnerStatus(currentStep);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OwnerVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitVerification() {
        ownerRepository.submitVerification(new RepositoryCallback<OwnerProfileDto>() {
            @Override
            public void onSuccess(OwnerProfileDto data) {
                ownerRepository.fetchStatus(new RepositoryCallback<OwnerStatusDataDto>() {
                    @Override
                    public void onSuccess(OwnerStatusDataDto statusData) {
                        sessionManager.updateOwnerCache(statusData);
                        startActivity(OwnerVerificationStatusActivity.pendingIntent(OwnerVerificationActivity.this));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        startActivity(OwnerVerificationStatusActivity.pendingIntent(OwnerVerificationActivity.this));
                        finish();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OwnerVerificationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindUpload(View root, int viewId, String documentType) {
        View view = root.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> pickDocument(documentType));
            if (uploadedDocs.contains(documentType)) {
                view.setAlpha(0.7f);
            }
        }
    }

    private void pickDocument(String documentType) {
        pendingDocumentType = documentType;
        documentPicker.launch(new String[]{"image/*", "application/pdf"});
    }

    private void uploadDocument(Uri uri, String documentType) {
        try {
            File temp = File.createTempFile("owner_doc_", ".jpg", getCacheDir());
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(temp)) {
                if (in == null) {
                    throw new IllegalStateException("Unable to read file");
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            RequestBody requestFile = RequestBody.create(temp, MediaType.parse("image/*"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", temp.getName(), requestFile);
            ownerRepository.uploadDocument(documentType, part, new RepositoryCallback<OwnerDocumentDto>() {
                @Override
                public void onSuccess(OwnerDocumentDto data) {
                    uploadedDocs.add(documentType);
                    Toast.makeText(OwnerVerificationActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    showStep(currentStep);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(OwnerVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void useCurrentLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("latitude", location.getLatitude());
            body.put("longitude", location.getLongitude());
            saveStep(2, body, false);
        });
    }
}

package com.parkshare.frontend.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.parkshare.api.models.OwnerStatusDataDto;
import com.parkshare.api.models.OwnerStatusDto;
import com.parkshare.api.models.UserDto;
import com.parkshare.frontend.activities.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    public static final String MODE_DRIVER = "driver";
    public static final String MODE_OWNER = "owner";

    private static final String PREFS = "parkshare_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_CURRENT_MODE = "current_mode";
    private static final String KEY_OWNER_CAPABILITY = "owner_capability";
    private static final String KEY_OWNER_STATUS = "owner_status";
    private static final String KEY_OWNER_CURRENT_STEP = "owner_current_step";
    private static final String KEY_OWNER_REJECTION = "owner_rejection_reason";
    private static final String KEY_OWNER_STEP_1 = "owner_step_1_status";
    private static final String KEY_OWNER_STEP_2 = "owner_step_2_status";
    private static final String KEY_OWNER_STEP_3 = "owner_step_3_status";
    private static final String KEY_OWNER_STEP_4 = "owner_step_4_status";

    private static volatile SessionManager instance;
    private final SharedPreferences prefs;
    private final List<Runnable> unauthorizedListeners = new ArrayList<>();

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    public void saveSession(String token, UserDto user) {
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_PHONE, user.getPhone())
                .putString(KEY_ROLE, user.getRole());

        updateOwnerCache(user, editor);

        if (!hasOwnerCapability()) {
            editor.putString(KEY_CURRENT_MODE, MODE_DRIVER);
        }

        editor.apply();
    }

    public void updateOwnerCache(UserDto user) {
        SharedPreferences.Editor editor = prefs.edit();
        updateOwnerCache(user, editor);
        editor.apply();
    }

    public void updateOwnerCache(OwnerStatusDataDto data) {
        if (data == null) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        if (data.getCapabilities() != null) {
            editor.putBoolean(KEY_OWNER_CAPABILITY, data.getCapabilities().isOwner());
        }
        if (data.getOwner() != null) {
            saveOwnerStatus(data.getOwner(), editor);
        }
        editor.apply();
    }

    private void updateOwnerCache(UserDto user, SharedPreferences.Editor editor) {
        if (user.getCapabilities() != null) {
            editor.putBoolean(KEY_OWNER_CAPABILITY, user.getCapabilities().isOwner());
        }
        if (user.getOwner() != null) {
            saveOwnerStatus(user.getOwner(), editor);
        }
    }

    private void saveOwnerStatus(OwnerStatusDto owner, SharedPreferences.Editor editor) {
        editor.putString(KEY_OWNER_STATUS, owner.getStatus() != null ? owner.getStatus() : "");
        editor.putInt(KEY_OWNER_CURRENT_STEP, owner.getCurrentStep());
        editor.putString(KEY_OWNER_REJECTION, owner.getRejectionReason() != null ? owner.getRejectionReason() : "");

        // Determine step statuses based on current_step and status
        int step = owner.getCurrentStep();
        String status = owner.getStatus();

        editor.putBoolean(KEY_OWNER_STEP_1, step > 1 || "under_review".equals(status) || "approved".equals(status));
        editor.putBoolean(KEY_OWNER_STEP_2, step > 2 || "under_review".equals(status) || "approved".equals(status));
        editor.putBoolean(KEY_OWNER_STEP_3, step > 3 || "under_review".equals(status) || "approved".equals(status));
        editor.putBoolean(KEY_OWNER_STEP_4, "under_review".equals(status) || "approved".equals(status));
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "driver");
    }

    public String getCurrentMode() {
        return prefs.getString(KEY_CURRENT_MODE, MODE_DRIVER);
    }

    public void setCurrentMode(String mode) {
        prefs.edit().putString(KEY_CURRENT_MODE, mode).apply();
    }

    public boolean isDriverMode() {
        return MODE_DRIVER.equals(getCurrentMode());
    }

    public boolean isOwnerMode() {
        return MODE_OWNER.equals(getCurrentMode());
    }

    public boolean hasOwnerCapability() {
        return prefs.getBoolean(KEY_OWNER_CAPABILITY, false);
    }

    @Nullable
    public String getOwnerStatus() {
        String status = prefs.getString(KEY_OWNER_STATUS, null);
        return status == null || status.isEmpty() ? null : status;
    }

    public int getOwnerCurrentStep() {
        return prefs.getInt(KEY_OWNER_CURRENT_STEP, 1);
    }

    @Nullable
    public String getOwnerRejectionReason() {
        String reason = prefs.getString(KEY_OWNER_REJECTION, null);
        return reason == null || reason.isEmpty() ? null : reason;
    }

    public boolean isDriver() {
        return "driver".equals(getRole()) || "owner".equals(getRole());
    }

    public boolean isOwner() {
        return hasOwnerCapability();
    }

    public boolean isTechnician() {
        return "technician".equals(getRole());
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public void addUnauthorizedListener(Runnable listener) {
        unauthorizedListeners.add(listener);
    }

    public void removeUnauthorizedListener(Runnable listener) {
        unauthorizedListeners.remove(listener);
    }

    public void notifyUnauthorized() {
        for (Runnable listener : new ArrayList<>(unauthorizedListeners)) {
            listener.run();
        }
    }

    public void redirectToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

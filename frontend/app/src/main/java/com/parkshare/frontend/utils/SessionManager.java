package com.parkshare.frontend.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.parkshare.api.models.UserDto;
import com.parkshare.frontend.activities.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final String PREFS = "parkshare_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";

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
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_PHONE, user.getPhone())
                .putString(KEY_ROLE, user.getRole())
                .apply();
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

    public boolean isDriver() {
        return "driver".equals(getRole());
    }

    public boolean isOwner() {
        return "owner".equals(getRole());
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

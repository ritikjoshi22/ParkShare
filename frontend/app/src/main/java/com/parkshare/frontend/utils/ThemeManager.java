package com.parkshare.frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private static final String PREFS = "parkshare_prefs";
    private static final String KEY_THEME = "theme_mode";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        applyMode(getThemeMode(context));
    }

    public static String getThemeMode(Context context) {
        return prefs(context).getString(KEY_THEME, MODE_SYSTEM);
    }

    public static void setThemeMode(Context context, String mode) {
        prefs(context).edit().putString(KEY_THEME, mode).apply();
        applyMode(mode);
    }

    private static void applyMode(String mode) {
        switch (mode) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

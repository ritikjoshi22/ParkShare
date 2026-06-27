package com.parkshare.frontend;

import android.app.Application;
import android.content.Context;

import com.parkshare.frontend.utils.ThemeManager;

public class ParkShareApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        ThemeManager.applySavedTheme(this);
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}

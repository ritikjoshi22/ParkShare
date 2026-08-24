package com.parkshare.frontend.utils;

import android.content.Context;
import android.content.Intent;

import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.activities.driver.DriverMainActivity;
import com.parkshare.frontend.activities.owner.OwnerMainActivity;
import com.parkshare.frontend.activities.technician.TechnicianMainActivity;

public final class RoleRouter {

    private RoleRouter() {
    }

    public static void openDashboard(Context context) {
        AppModeRouter.openInitialDashboard(context);
    }

    public static boolean isRoleAllowed(Context context, String requiredRole) {
        SessionManager session = SessionManager.getInstance(context);
        switch (requiredRole) {
            case "driver":
                return session.isDriver();
            case "owner":
                return session.isOwner();
            case "technician":
                return session.isTechnician();
            default:
                return false;
        }
    }
}

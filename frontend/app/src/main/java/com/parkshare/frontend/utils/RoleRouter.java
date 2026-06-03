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
        SessionManager session = SessionManager.getInstance(context);
        if (!session.isLoggedIn()) {
            context.startActivity(new Intent(context, LoginActivity.class));
            return;
        }
        Intent intent;
        if (session.isOwner()) {
            intent = new Intent(context, OwnerMainActivity.class);
        } else if (session.isTechnician()) {
            intent = new Intent(context, TechnicianMainActivity.class);
        } else {
            intent = new Intent(context, DriverMainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
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

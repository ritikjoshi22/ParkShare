package com.parkshare.frontend.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.parkshare.api.models.OwnerStatusDataDto;
import com.parkshare.frontend.activities.LoginActivity;
import com.parkshare.frontend.activities.driver.DriverMainActivity;
import com.parkshare.frontend.activities.owner.OwnerMainActivity;
import com.parkshare.frontend.activities.owner.OwnerVerificationActivity;
import com.parkshare.frontend.activities.owner.OwnerVerificationStatusActivity;
import com.parkshare.frontend.activities.technician.TechnicianMainActivity;
import com.parkshare.frontend.repository.OwnerRepository;

public final class AppModeRouter {

    private static boolean isLoading = false;

    private AppModeRouter() {
    }

    public static void openInitialDashboard(Context context) {
        SessionManager session = SessionManager.getInstance(context);
        if (!session.isLoggedIn()) {
            context.startActivity(new Intent(context, LoginActivity.class));
            return;
        }
        if (session.isTechnician()) {
            context.startActivity(new Intent(context, TechnicianMainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }
        if (session.isOwnerMode() && session.hasOwnerCapability()) {
            openOwnerDashboard(context);
        } else {
            openDriverDashboard(context);
        }
    }

    public static void openDriverDashboard(Context context) {
        SessionManager.getInstance(context).setCurrentMode(SessionManager.MODE_DRIVER);
        context.startActivity(new Intent(context, DriverMainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public static void openOwnerDashboard(Context context) {
        SessionManager session = SessionManager.getInstance(context);
        session.setCurrentMode(SessionManager.MODE_OWNER);
        context.startActivity(new Intent(context, OwnerMainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public static void handleSwitchToOwner(Context context) {
        if (isLoading) return;

        SessionManager session = SessionManager.getInstance(context);
        String cachedStatus = session.getOwnerStatus();

        if (session.hasOwnerCapability() || "approved".equals(cachedStatus)) {
            openOwnerDashboard(context);
            return;
        }

        if ("under_review".equals(cachedStatus) || "submitted".equals(cachedStatus)) {
            context.startActivity(OwnerVerificationStatusActivity.pendingIntent(context));
            return;
        }
        if ("rejected".equals(cachedStatus)) {
            context.startActivity(OwnerVerificationActivity.intent(
                    context, session.getOwnerCurrentStep(), session.getOwnerRejectionReason()));
            return;
        }
        if ("draft".equals(cachedStatus)) {
            context.startActivity(OwnerVerificationActivity.intent(context, session.getOwnerCurrentStep(), null));
            return;
        }

        isLoading = true;
        new OwnerRepository().fetchStatus(new RepositoryCallback<OwnerStatusDataDto>() {
            @Override
            public void onSuccess(OwnerStatusDataDto data) {
                isLoading = false;
                session.updateOwnerCache(data);
                routeOwnerSwitch(context, data, session);
            }

            @Override
            public void onError(String message) {
                isLoading = false;
                Toast.makeText(context, message != null ? message : "Unable to load owner status", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void routeOwnerSwitch(Context context, OwnerStatusDataDto data, SessionManager session) {
        if (data.getCapabilities() != null && data.getCapabilities().isOwner()) {
            openOwnerDashboard(context);
            return;
        }
        String status = data.getOwner() != null ? data.getOwner().getStatus() : null;
        if (status == null) {
            context.startActivity(OwnerVerificationActivity.intent(context, 1, null));
            return;
        }
        switch (status) {
            case "approved":
                openOwnerDashboard(context);
                break;
            case "under_review":
            case "submitted":
                context.startActivity(OwnerVerificationStatusActivity.pendingIntent(context));
                break;
            case "rejected":
                context.startActivity(OwnerVerificationActivity.intent(
                        context,
                        data.getOwner().getCurrentStep(),
                        data.getOwner().getRejectionReason()));
                break;
            default:
                context.startActivity(OwnerVerificationActivity.intent(
                        context,
                        data.getOwner() != null ? data.getOwner().getCurrentStep() : 1,
                        null));
                break;
        }
    }
}

package com.parkshare.frontend.activities.owner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.parkshare.frontend.R;
import com.parkshare.frontend.utils.AppModeRouter;

public class OwnerVerificationStatusActivity extends AppCompatActivity {

    public static Intent pendingIntent(Context context) {
        Intent intent = new Intent(context, OwnerVerificationStatusActivity.class);
        intent.putExtra("pending", true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_verification_status);

        TextView title = findViewById(R.id.tvTitle);
        TextView message = findViewById(R.id.tvMessage);
        title.setText(R.string.verification_submitted_title);
        message.setText(R.string.verification_pending_message);

        MaterialButton back = findViewById(R.id.btnBackToProfile);
        back.setOnClickListener(v -> {
            AppModeRouter.openDriverDashboard(this);
            finish();
        });
    }
}

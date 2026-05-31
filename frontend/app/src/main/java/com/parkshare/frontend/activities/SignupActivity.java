package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    public static final String EXTRA_FULL_NAME = "full_name";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_PASSWORD = "password";

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> {
            String fullName = textOf(binding.etFullName);
            String phone = textOf(binding.etPhone);
            String email = textOf(binding.etEmail);
            String password = textOf(binding.etPassword);
            String confirm = textOf(binding.etConfirmPassword);

            if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SignupActivity.this, RoleSelectionActivity.class);
            intent.putExtra(EXTRA_FULL_NAME, fullName);
            intent.putExtra(EXTRA_EMAIL, email);
            intent.putExtra(EXTRA_PHONE, phone);
            intent.putExtra(EXTRA_PASSWORD, password);
            startActivity(intent);
        });

        binding.tvLoginRedirect.setOnClickListener(v -> finish());
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}

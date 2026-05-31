package com.parkshare.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parkshare.frontend.databinding.ActivityLoginBinding;
import com.parkshare.frontend.repository.AuthRepository;
import com.parkshare.frontend.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        authRepository = new AuthRepository(sessionManager);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvSignupRedirect.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contact support to reset your password", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null
                ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null
                ? binding.etPassword.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        authRepository.login(email, password, new com.parkshare.frontend.utils.RepositoryCallback<com.parkshare.api.models.UserDto>() {
            @Override
            public void onSuccess(com.parkshare.api.models.UserDto data) {
                setLoading(false);
                goToMain();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? getString(com.parkshare.frontend.R.string.loading) : getString(com.parkshare.frontend.R.string.login));
    }

    private void goToMain() {
        com.parkshare.frontend.utils.RoleRouter.openDashboard(this);
        finish();
    }
}

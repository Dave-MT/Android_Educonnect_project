package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.LoginRequest;
import com.educonnect.model.LoginResponse;
import com.educonnect.model.User;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToAppropriateActivity();
            finish();
            return;
        }

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Set up login button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(email, password);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.getStatus().equals("success")) {
                        String token = loginResponse.getToken();
                        User user = loginResponse.getUser();
                        
                        // Extract role from token
                        String role = JwtUtils.extractRole(token);
                        
                        // Create session
                        sessionManager.createSession(token, user, role);
                        
                        // Navigate to appropriate activity based on role
                        navigateToAppropriateActivity();
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Login error: " + errorBody);
                        Toast.makeText(LoginActivity.this, "Login failed: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error during login", t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToAppropriateActivity() {
        if (sessionManager.isTeacher()) {
            Intent intent = new Intent(LoginActivity.this, TeacherMenuActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(LoginActivity.this, StudentMenuActivity.class);
            startActivity(intent);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}

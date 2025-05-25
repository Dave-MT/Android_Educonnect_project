package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ProfileResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";
    
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvWelcome;
    private TextView tvEmail;
    private TextView tvRole;
    private TextView tvCreatedAt;
    private Button btnLogout;
    private Button btnContinue;
    private TextView tvErrorMessage;
    private CardView cardViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        cardViewProfile = findViewById(R.id.cardViewProfile);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        btnLogout = findViewById(R.id.btnLogout);
        btnContinue = findViewById(R.id.btnContinue);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        
        // Set up toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }
        
        // Set up logout button
        btnLogout.setOnClickListener(v -> logout());
        
        // Set up continue button to navigate to student menu
        btnContinue.setOnClickListener(v -> {
            navigateToDashboard();
        });
        
        // Load profile
        fetchProfile();
    }
    
    // Enhance the fetchProfile method to properly handle the API response
    private void fetchProfile() {
        showLoading(true);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();
        
        Log.d(TAG, "Fetching profile with token: " + token);
        
        Call<ProfileResponse> call = apiService.getProfile(token);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                showLoading(false);
            
                Log.d(TAG, "Profile response code: " + response.code());
            
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    Log.d(TAG, "Profile response status: " + profileResponse.getStatus());
                
                    if (profileResponse.getStatus().equals("success")) {
                        displayProfile(profileResponse);
                    } else {
                        showError(profileResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching profile: " + errorBody);
                        showError("Failed to load profile: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load profile");
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching profile", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // Improve the displayProfile method to handle the profile data correctly
    private void displayProfile(ProfileResponse profileResponse) {
        ProfileResponse.ProfileData data = profileResponse.getData();
    
        if (data == null) {
            showError("No profile data available");
            return;
        }
        
        // Show the profile card
        cardViewProfile.setVisibility(View.VISIBLE);
    
        // Set welcome message
        String firstName = data.getFirstName() != null ? data.getFirstName() : "";
        String lastName = data.getLastName() != null ? data.getLastName() : "";
        String welcomeText;
    
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            welcomeText = "Welcome, " + firstName + " " + lastName + "!";
        } else {
            // Fallback to email if name is not available
            welcomeText = "Welcome, " + data.getEmail() + "!";
        }
        tvWelcome.setText(welcomeText);
    
        // Set email and role
        tvEmail.setText(data.getEmail());
        tvRole.setText(data.getRole());
    
        // Format and set created date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
            Date date = inputFormat.parse(data.getCreatedAt());
            tvCreatedAt.setText(outputFormat.format(date));
        } catch (ParseException e) {
            tvCreatedAt.setText(data.getCreatedAt());
        }
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvLoadingMessage.setVisibility(View.VISIBLE);
            cardViewProfile.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            tvLoadingMessage.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_profile);
    }
}

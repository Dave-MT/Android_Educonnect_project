package com.educonnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.adapter.GradeAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.GradeResponse;
import com.educonnect.utils.JwtUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckGradesActivity extends BaseActivity {
    private static final String TAG = "CheckGradesActivity";

    private RecyclerView recyclerView;
    private GradeAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvErrorMessage;
    private TextView tvNoGrades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_grades);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvNoGrades = findViewById(R.id.tvNoGrades);

        // Set up toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Grades");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter(this, this::openSubmissionFile);
        recyclerView.setAdapter(adapter);

        // Load grades
        loadGrades();
    }

    private void loadGrades() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = sessionManager.getToken();

        // Get user ID from token
        int userId = JwtUtils.getUserIdFromToken(token);
        if (userId == -1) {
            showError("Invalid user information. Please log in again.");
            return;
        }

        Call<GradeResponse> call = apiService.getGrades("Bearer " + token, userId);
        call.enqueue(new Callback<GradeResponse>() {
            @Override
            public void onResponse(Call<GradeResponse> call, Response<GradeResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    GradeResponse gradeResponse = response.body();

                    if (gradeResponse.getStatus().equals("success")) {
                        if (gradeResponse.getData() != null && !gradeResponse.getData().isEmpty()) {
                            adapter.setGrades(gradeResponse.getData());
                            tvNoGrades.setVisibility(View.GONE);
                        } else {
                            tvNoGrades.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError(gradeResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching grades: " + errorBody);
                        showError("Failed to load grades: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load grades");
                    }
                }
            }

            @Override
            public void onFailure(Call<GradeResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching grades", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private String constructDownloadUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // Get base URL without /educonnect/api
        String baseUrl = RetrofitClient.getBaseUrl().replace("/educonnect/api", "");

        // Clean the file path
        String cleanPath = filePath;

        // Remove server-specific paths
        if (cleanPath.contains("/var/www/html/")) {
            cleanPath = cleanPath.substring(cleanPath.indexOf("uploads/"));
        }

        // Remove leading slashes
        cleanPath = cleanPath.replaceAll("^/+", "");

        // Extract filename if it's a full path
        if (cleanPath.contains("/")) {
            String[] parts = cleanPath.split("/");
            cleanPath = "uploads/" + parts[parts.length - 1];
        } else if (!cleanPath.startsWith("uploads/")) {
            cleanPath = "uploads/" + cleanPath;
        }

        // Construct final URL
        return baseUrl + "/educonnect/" + cleanPath;
    }

    private void openSubmissionFile(String fileUrl) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            try {
                String downloadUrl = constructDownloadUrl(fileUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(downloadUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening file", e);
                Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "File not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvLoadingMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
            tvNoGrades.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            tvLoadingMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoGrades.setVisibility(View.GONE);
    }

    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_grades);
    }
}

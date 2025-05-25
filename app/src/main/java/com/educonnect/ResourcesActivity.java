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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.adapter.ResourceAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ResourceResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourcesActivity extends BaseActivity {
    private static final String TAG = "ResourcesActivity";

    private RecyclerView recyclerView;
    private ResourceAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvErrorMessage;
    private TextView tvNoResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvNoResources = findViewById(R.id.tvNoResources);

        // Set up toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Resources");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourceAdapter(this, this::openResourceFile, false); // false for student
        recyclerView.setAdapter(adapter);

        // Load resources
        loadResources();
    }

    private void loadResources() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<ResourceResponse> call = apiService.getResources(token);
        call.enqueue(new Callback<ResourceResponse>() {
            @Override
            public void onResponse(Call<ResourceResponse> call, Response<ResourceResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ResourceResponse resourceResponse = response.body();

                    if (resourceResponse.getStatus().equals("success")) {
                        if (resourceResponse.getData() != null && !resourceResponse.getData().isEmpty()) {
                            adapter.setResources(resourceResponse.getData());
                            tvNoResources.setVisibility(View.GONE);
                        } else {
                            tvNoResources.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError(resourceResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching resources: " + errorBody);
                        showError("Failed to load resources: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load resources");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResourceResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching resources", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void openResourceFile(String fileUrl) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            // Check if the URL is already complete
            String finalUrl;
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                // URL is already complete, use as-is
                finalUrl = fileUrl;
            } else {
                // URL is relative, construct the full URL
                String baseUrl = RetrofitClient.getBaseUrl().replace("/educonnect/api", "");
                finalUrl = baseUrl + "/educonnect/" + fileUrl.replace("uploads/", "uploads/");
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
            startActivity(browserIntent);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvLoadingMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
            tvNoResources.setVisibility(View.GONE);
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
        tvNoResources.setVisibility(View.GONE);
    }

    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_resources);
    }
}


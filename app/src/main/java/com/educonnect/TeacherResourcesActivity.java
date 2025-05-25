package com.educonnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.educonnect.adapter.ResourceAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ResourceResponse;
import com.educonnect.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherResourcesActivity extends BaseActivity implements ResourceAdapter.FileClickListener, ResourceAdapter.OnResourceDeletedListener {
    private static final String TAG = "TeacherResourcesActivity";
    private RecyclerView recyclerView;
    private ResourceAdapter resourceAdapter;
    private List<ResourceResponse.Resource> resourceList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoResources;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_resources);

        sessionManager = new SessionManager(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoResources = findViewById(R.id.tvNoResources);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAdd = findViewById(R.id.fabAdd);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceAdapter = new ResourceAdapter(this, this, true); // true for teacher
        resourceAdapter.setOnResourceDeletedListener(this);
        resourceAdapter.setResources(resourceList);
        recyclerView.setAdapter(resourceAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadResources);

        // Set up add button
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherResourcesActivity.this, CreateResourceActivity.class);
            startActivity(intent);
        });

        // Load resources
        loadResources();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload resources when returning to this activity
        loadResources();
    }

    private void loadResources() {
        showLoading(true);
        tvNoResources.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<ResourceResponse> call = apiService.getTeacherResources(token);
        call.enqueue(new Callback<ResourceResponse>() {
            @Override
            public void onResponse(Call<ResourceResponse> call, Response<ResourceResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ResourceResponse resourceResponse = response.body();
                    if (resourceResponse.getStatus().equals("success") && resourceResponse.getData() != null) {
                        resourceList.clear();
                        resourceList.addAll(resourceResponse.getData());
                        resourceAdapter.setResources(resourceList);

                        if (resourceList.isEmpty()) {
                            tvNoResources.setVisibility(View.VISIBLE);
                        } else {
                            tvNoResources.setVisibility(View.GONE);
                        }
                    } else {
                        tvNoResources.setVisibility(View.VISIBLE);
                        Toast.makeText(TeacherResourcesActivity.this,
                                "Error: " + resourceResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvNoResources.setVisibility(View.VISIBLE);
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading resources: " + errorBody);
                        Toast.makeText(TeacherResourcesActivity.this,
                                "Failed to load resources: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResourceResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                tvNoResources.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network error loading resources", t);
                Toast.makeText(TeacherResourcesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFileClick(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onResourceDeleted() {
        // Reload resources after deletion
        loadResources();
    }
}

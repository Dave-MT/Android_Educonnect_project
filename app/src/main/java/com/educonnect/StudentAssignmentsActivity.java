package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.adapter.StudentAssignmentAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.AssignmentResponse;
import com.educonnect.utils.JwtUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssignmentsActivity extends BaseActivity {
    private static final String TAG = "StudentAssignmentsActivity";

    private RecyclerView recyclerView;
    private StudentAssignmentAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvErrorMessage;
    private TextView tvNoAssignments;
    private List<AssignmentResponse.Assignment> assignmentList = new ArrayList<>();
    private ActivityResultLauncher<Intent> submitAssignmentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignments);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvNoAssignments = findViewById(R.id.tvNoAssignments);

        // Set up toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Assignments");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAssignmentAdapter(assignmentList, this);
        recyclerView.setAdapter(adapter);

        // Set up activity result launcher for assignment submission
        submitAssignmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Reload assignments when returning from successful submission
                        loadAssignments();
                    }
                }
        );

        // Load assignments
        loadAssignments();
    }

    private void loadAssignments() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = sessionManager.getToken();

        // Get user ID from token
        int userId = JwtUtils.getUserIdFromToken(token);
        if (userId == -1) {
            showError("Invalid user information. Please log in again.");
            return;
        }

        Call<AssignmentResponse> call = apiService.getAssignments("Bearer " + token, userId);
        call.enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AssignmentResponse assignmentResponse = response.body();

                    if (assignmentResponse.getStatus().equals("success")) {
                        if (assignmentResponse.getData() != null && !assignmentResponse.getData().isEmpty()) {
                            assignmentList.clear();
                            assignmentList.addAll(assignmentResponse.getData());
                            adapter.notifyDataSetChanged();
                            tvNoAssignments.setVisibility(View.GONE);
                        } else {
                            tvNoAssignments.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError(assignmentResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching assignments: " + errorBody);
                        showError("Failed to load assignments: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load assignments");
                    }
                }
            }

            @Override
            public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching assignments", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvLoadingMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
            tvNoAssignments.setVisibility(View.GONE);
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
        tvNoAssignments.setVisibility(View.GONE);
    }

    public ActivityResultLauncher<Intent> getSubmitAssignmentLauncher() {
        return submitAssignmentLauncher;
    }

    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_assignments);
    }
}

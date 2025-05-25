package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.educonnect.adapter.SubmissionAdapter;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Submission;
import com.educonnect.model.SubmissionResponse;
import com.educonnect.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeSubmissionsActivity extends BaseActivity {
    private static final String TAG = "GradeSubmissionsActivity";

    private RecyclerView recyclerView;
    private SubmissionAdapter adapter;
    private List<Submission> submissionList;
    private ProgressBar progressBar;
    private TextView tvNoSubmissions;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int assignmentId;
    private String assignmentTitle;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_submissions);

        // Get assignment ID from intent
        assignmentId = getIntent().getIntExtra("assignment_id", -1);
        assignmentTitle = getIntent().getStringExtra("assignment_title");

        if (assignmentId == -1) {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(assignmentTitle != null ? "Grade: " + assignmentTitle : "Grade Submissions");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewSubmissions);
        progressBar = findViewById(R.id.progressBar);
        tvNoSubmissions = findViewById(R.id.tvNoSubmissions);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionList = new ArrayList<>();
        adapter = new SubmissionAdapter(this, submissionList);
        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadSubmissions);

        // Load submissions
        loadSubmissions();
    }

    private void loadSubmissions() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoSubmissions.setVisibility(View.GONE);

        String token = "Bearer " + sessionManager.getToken();
        RetrofitClient.getClient().create(com.educonnect.api.ApiService.class)
                .getSubmissions(token, assignmentId)
                .enqueue(new Callback<SubmissionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubmissionResponse> call, @NonNull Response<SubmissionResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.code() == 401) {
                            // Token expired, redirect to login
                            Toast.makeText(GradeSubmissionsActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                            sessionManager.logout();
                            Intent intent = new Intent(GradeSubmissionsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            submissionList.clear();

                            List<Submission> submissions = response.body().getData();
                            if (submissions != null && !submissions.isEmpty()) {
                                submissionList.addAll(submissions);
                                adapter.notifyDataSetChanged();
                                tvNoSubmissions.setVisibility(View.GONE);
                            } else {
                                tvNoSubmissions.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvNoSubmissions.setVisibility(View.VISIBLE);
                            tvNoSubmissions.setText(R.string.error_loading_submissions);
                            Log.e(TAG, "Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubmissionResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        tvNoSubmissions.setVisibility(View.VISIBLE);
                        tvNoSubmissions.setText(R.string.error_loading_submissions);
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

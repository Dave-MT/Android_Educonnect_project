package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.educonnect.adapter.TeacherAssignmentAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.model.AssignmentResponse;
import com.educonnect.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAssignmentsActivity extends BaseActivity {
    private static final String TAG = "TeacherAssignmentsActivity";
    private RecyclerView recyclerView;
    private TeacherAssignmentAdapter assignmentAdapter;
    private List<Assignment> assignmentList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoAssignments;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_assignments);

        sessionManager = new SessionManager(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoAssignments = findViewById(R.id.tvNoAssignments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAdd = findViewById(R.id.fabAdd);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignmentAdapter = new TeacherAssignmentAdapter(assignmentList, this);
        recyclerView.setAdapter(assignmentAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadAssignments);

        // Set up add button
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherAssignmentsActivity.this, CreateAssignmentActivity.class);
            startActivity(intent);
        });

        // Load assignments
        loadAssignments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload assignments when returning to this activity
        loadAssignments();
    }

    private void loadAssignments() {
        showLoading(true);
        tvNoAssignments.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<AssignmentResponse> call = apiService.getTeacherAssignments(token);
        call.enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AssignmentResponse assignmentResponse = response.body();
                    if (assignmentResponse.getStatus().equals("success") && assignmentResponse.getData() != null) {
                        assignmentList.clear();

                        // Convert AssignmentResponse.Assignment to Assignment
                        for (AssignmentResponse.Assignment responseAssignment : assignmentResponse.getData()) {
                            Assignment assignment = new Assignment();
                            assignment.setId(responseAssignment.getId());
                            assignment.setTitle(responseAssignment.getTitle());
                            assignment.setDescription(responseAssignment.getDescription());
                            assignment.setClassName(responseAssignment.getClassName());
                            assignment.setDueDate(responseAssignment.getDeadline());
                            assignment.setDeadline(responseAssignment.getDeadline());
                            assignment.setClassId(responseAssignment.getClassId());
                            assignment.setTeacherId(responseAssignment.getTeacherId());
                            assignment.setFile(responseAssignment.getFile());

                            assignmentList.add(assignment);
                        }

                        assignmentAdapter.notifyDataSetChanged();

                        if (assignmentList.isEmpty()) {
                            tvNoAssignments.setVisibility(View.VISIBLE);
                        } else {
                            tvNoAssignments.setVisibility(View.GONE);
                        }
                    } else {
                        tvNoAssignments.setVisibility(View.VISIBLE);
                        Toast.makeText(TeacherAssignmentsActivity.this,
                                "Error: " + assignmentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvNoAssignments.setVisibility(View.VISIBLE);
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading assignments: " + errorBody);
                        Toast.makeText(TeacherAssignmentsActivity.this,
                                "Failed to load assignments: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                tvNoAssignments.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network error loading assignments", t);
                Toast.makeText(TeacherAssignmentsActivity.this,
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
}

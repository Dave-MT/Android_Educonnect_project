package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.educonnect.adapter.AssignmentAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassDetailActivity extends AppCompatActivity {
    private static final String TAG = "ClassDetailActivity";
    private RecyclerView recyclerView;
    private AssignmentAdapter assignmentAdapter;
    private List<Assignment> assignmentList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvClassName;
    private TextView tvNoAssignments;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabRefresh;
    private int classId;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        sessionManager = new SessionManager(this);

        // Get class ID from intent
        classId = getIntent().getIntExtra("CLASS_ID", -1);
        className = getIntent().getStringExtra("CLASS_NAME");

        if (classId == -1) {
            Toast.makeText(this, "Invalid class", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvClassName = findViewById(R.id.tvClassName);
        tvNoAssignments = findViewById(R.id.tvNoAssignments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabRefresh = findViewById(R.id.fabRefresh);

        // Set class name
        tvClassName.setText(className);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignmentAdapter = new AssignmentAdapter(assignmentList, this);
        recyclerView.setAdapter(assignmentAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadAssignments);
        
        // Set up refresh button
        fabRefresh.setOnClickListener(v -> loadAssignments());

        // Load assignments
        loadAssignments();
    }

    private void loadAssignments() {
        showLoading(true);
        tvNoAssignments.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();
        
        Log.d(TAG, "Loading assignments for class ID: " + classId);

        Call<List<Assignment>> call = apiService.getAssignmentsByClass(classId, token);
        call.enqueue(new Callback<List<Assignment>>() {
            @Override
            public void onResponse(Call<List<Assignment>> call, Response<List<Assignment>> response) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                
                Log.d(TAG, "Assignments response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    assignmentList.clear();
                    assignmentList.addAll(response.body());
                    assignmentAdapter.notifyDataSetChanged();
                    
                    Log.d(TAG, "Loaded " + assignmentList.size() + " assignments");

                    if (assignmentList.isEmpty()) {
                        tvNoAssignments.setVisibility(View.VISIBLE);
                    } else {
                        tvNoAssignments.setVisibility(View.GONE);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading assignments: " + errorBody);
                        Toast.makeText(ClassDetailActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                        tvNoAssignments.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Assignment>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Log.e(TAG, "Network error loading assignments", t);
                Toast.makeText(ClassDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvNoAssignments.setVisibility(View.VISIBLE);
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

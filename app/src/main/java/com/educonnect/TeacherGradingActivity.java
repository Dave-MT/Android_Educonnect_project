package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.model.AssignmentResponse;
import com.educonnect.model.ClassResponse;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherGradingActivity extends BaseActivity {
    private static final String TAG = "TeacherGradingActivity";
    
    private Spinner spinnerClass;
    private ListView listViewAssignments;
    private ProgressBar progressBar;
    private TextView tvNoAssignments;
    
    private SessionManager sessionManager;
    private List<ClassResponse.ClassData> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private List<Integer> classIds = new ArrayList<>();
    private List<Assignment> assignmentList = new ArrayList<>();
    private ArrayAdapter<String> assignmentAdapter;
    private int selectedClassId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_grading);
        
        sessionManager = new SessionManager(this);
        
        // Initialize views
        spinnerClass = findViewById(R.id.spinnerClass);
        listViewAssignments = findViewById(R.id.listViewAssignments);
        progressBar = findViewById(R.id.progressBar);
        tvNoAssignments = findViewById(R.id.tvNoAssignments);
        
        // Set up adapter for assignments - changed to use String adapter
        List<String> assignmentTitles = new ArrayList<>();
        assignmentAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_list_item_1, 
                assignmentTitles);
        listViewAssignments.setAdapter(assignmentAdapter);
        
        // Load classes for spinner
        loadClasses();
        
        // Set up class selection listener
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < classIds.size()) {
                    selectedClassId = classIds.get(position);
                    loadAssignments(selectedClassId);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Set up assignment selection listener
        listViewAssignments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < assignmentList.size()) {
                    Assignment assignment = assignmentList.get(position);
                    if (assignment != null) {
                        Intent intent = new Intent(TeacherGradingActivity.this, GradeSubmissionsActivity.class);
                        intent.putExtra("assignment_id", assignment.getId());
                        intent.putExtra("assignment_title", assignment.getTitle());
                        startActivity(intent);
                    }
                }
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload assignments when returning to this activity
        if (selectedClassId != -1) {
            loadAssignments(selectedClassId);
        }
    }
    
    private void loadClasses() {
        showLoading(true);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();
        
        Call<ClassResponse> call = apiService.getClasses(token);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ClassResponse classResponse = response.body();
                    if (classResponse.getStatus().equals("success") && classResponse.getData() != null) {
                        classList.clear();
                        classNames.clear();
                        classIds.clear();
                        
                        classList.addAll(classResponse.getData());
                        
                        for (ClassResponse.ClassData classData : classList) {
                            if (classData != null && classData.getClassName() != null) {
                                classNames.add(classData.getClassName());
                                classIds.add(classData.getId());
                            }
                        }
                        
                        // Set up spinner adapter
                        if (!classNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    TeacherGradingActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    classNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerClass.setAdapter(adapter);
                        } else {
                            Toast.makeText(TeacherGradingActivity.this, 
                                    "No classes available", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TeacherGradingActivity.this, 
                                "Error: " + classResponse.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading classes: " + errorBody);
                        Toast.makeText(TeacherGradingActivity.this, 
                                "Failed to load classes: " + response.code(), 
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading classes", t);
                Toast.makeText(TeacherGradingActivity.this, 
                        "Network error: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadAssignments(int classId) {
        showLoading(true);
        tvNoAssignments.setVisibility(View.GONE);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();
        int teacherId = JwtUtils.extractUserId(sessionManager.getToken());
        
        Call<AssignmentResponse> call = apiService.getTeacherAssignmentsByClass(token, classId, teacherId);
        call.enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Call<AssignmentResponse> call, Response<AssignmentResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    AssignmentResponse assignmentResponse = response.body();
                    if (assignmentResponse.getStatus().equals("success") && assignmentResponse.getData() != null) {
                        assignmentList.clear();
                        List<String> assignmentTitles = new ArrayList<>();
                        
                        // Convert AssignmentResponse.Assignment to Assignment
                        for (AssignmentResponse.Assignment responseAssignment : assignmentResponse.getData()) {
                            Assignment assignment = new Assignment();
                            assignment.setId(responseAssignment.getId());
                            assignment.setTitle(responseAssignment.getTitle());
                            assignment.setDescription(responseAssignment.getDescription());
                            
                            // Add to our lists
                            assignmentList.add(assignment);
                            assignmentTitles.add(responseAssignment.getTitle());
                        }
                        
                        // Update adapter with new titles
                        assignmentAdapter.clear();
                        assignmentAdapter.addAll(assignmentTitles);
                        assignmentAdapter.notifyDataSetChanged();
                        
                        if (assignmentList.isEmpty()) {
                            tvNoAssignments.setVisibility(View.VISIBLE);
                        } else {
                            tvNoAssignments.setVisibility(View.GONE);
                        }
                    } else {
                        tvNoAssignments.setVisibility(View.VISIBLE);
                        Toast.makeText(TeacherGradingActivity.this, 
                                "Error: " + assignmentResponse.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvNoAssignments.setVisibility(View.VISIBLE);
                    try {
                        String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading assignments: " + errorBody);
                        Toast.makeText(TeacherGradingActivity.this, 
                                "Failed to load assignments: " + response.code(), 
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<AssignmentResponse> call, Throwable t) {
                showLoading(false);
                tvNoAssignments.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network error loading assignments", t);
                Toast.makeText(TeacherGradingActivity.this, 
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

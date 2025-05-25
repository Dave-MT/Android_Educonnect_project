package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.model.SubmissionRequest;
import com.educonnect.model.SubmissionResponse;
import com.educonnect.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentSubmissionActivity extends AppCompatActivity {
    private static final String TAG = "SubmissionActivity";
    private TextView tvAssignmentTitle;
    private TextView tvAssignmentDescription;
    private TextView tvDueDate;
    private EditText etSubmissionText;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private int assignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_submission);

        sessionManager = new SessionManager(this);

        // Get assignment ID from intent
        assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
        if (assignmentId == -1) {
            Toast.makeText(this, "Invalid assignment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvAssignmentTitle = findViewById(R.id.tvAssignmentTitle);
        tvAssignmentDescription = findViewById(R.id.tvAssignmentDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        etSubmissionText = findViewById(R.id.etSubmissionText);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Load assignment details
        loadAssignmentDetails();

        // Set up submit button
        btnSubmit.setOnClickListener(v -> submitAssignment());
    }

    private void loadAssignmentDetails() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<Assignment> call = apiService.getAssignmentById(assignmentId, token);
        call.enqueue(new Callback<Assignment>() {
            @Override
            public void onResponse(Call<Assignment> call, Response<Assignment> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Assignment assignment = response.body();
                    tvAssignmentTitle.setText(assignment.getTitle());
                    tvAssignmentDescription.setText(assignment.getDescription());
                    tvDueDate.setText("Due: " + assignment.getDueDate());
                } else {
                    Toast.makeText(AssignmentSubmissionActivity.this, "Failed to load assignment details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Assignment> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AssignmentSubmissionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitAssignment() {
        String submissionText = etSubmissionText.getText().toString().trim();
        if (submissionText.isEmpty()) {
            Toast.makeText(this, "Please enter your submission", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        SubmissionRequest submissionRequest = new SubmissionRequest(
                assignmentId,
                sessionManager.getUser().getId(),
                submissionText
        );

        Call<SubmissionResponse> call = apiService.submitAssignment(submissionRequest, token);
        call.enqueue(new Callback<SubmissionResponse>() {
            @Override
            public void onResponse(Call<SubmissionResponse> call, Response<SubmissionResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    SubmissionResponse submissionResponse = response.body();
                    if (submissionResponse.getStatus().equals("success")) {
                        Toast.makeText(AssignmentSubmissionActivity.this, "Assignment submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AssignmentSubmissionActivity.this, submissionResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AssignmentSubmissionActivity.this, "Failed to submit assignment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubmissionResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AssignmentSubmissionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
        }
    }
}

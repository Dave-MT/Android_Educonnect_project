package com.educonnect.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.R;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.GradeRequest;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.Submission;
import com.educonnect.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder> {
    private final Context context;
    private final List<Submission> submissionList;
    private final SessionManager sessionManager;
    private static final String BASE_URL = "https://dc75-102-208-97-134.ngrok-free.app/educonnect/";

    public SubmissionAdapter(Context context, List<Submission> submissionList) {
        this.context = context;
        this.submissionList = submissionList;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_submission, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        Submission submission = submissionList.get(position);

        // Set student name
        holder.tvStudentName.setText(submission.getStudentName());

        // Set submission date
        holder.tvSubmissionDate.setText(String.format("Submitted: %s", submission.getSubmissionDate()));

        // Set current grade if available
        if (submission.getGrade() != null && !submission.getGrade().isEmpty()) {
            holder.tvCurrentGrade.setVisibility(View.VISIBLE);
            holder.tvCurrentGrade.setText(String.format("Current Grade: %s", submission.getGrade()));
        } else {
            holder.tvCurrentGrade.setVisibility(View.GONE);
        }

        // Set feedback if available
        if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
            holder.tvFeedback.setVisibility(View.VISIBLE);
            holder.tvFeedback.setText(String.format("Feedback: %s", submission.getFeedback()));
        } else {
            holder.tvFeedback.setVisibility(View.GONE);
        }

        // Set file button visibility and functionality
        if (submission.getFileUrl() != null && !submission.getFileUrl().isEmpty()) {
            holder.btnViewFile.setVisibility(View.VISIBLE);
            holder.btnViewFile.setOnClickListener(v -> downloadFile(submission.getFileUrl()));
        } else {
            holder.btnViewFile.setVisibility(View.GONE);
        }

        // Set grade button click listener
        holder.btnGrade.setOnClickListener(v -> showGradeDialog(submission));
    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    private void downloadFile(String fileUrl) {
        try {
            // Construct the full URL
            String fullUrl;
            if (fileUrl.startsWith("http")) {
                // Already a full URL
                fullUrl = fileUrl;
            } else {
                // Relative path, construct full URL
                fullUrl = BASE_URL + fileUrl;
            }

            // Extract filename from the URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            if (fileName.isEmpty()) {
                fileName = "submission_file";
            }

            // Use DownloadManager to download the file
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fullUrl));

            // Set download destination
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            // Set notification settings
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("Downloading " + fileName);
            request.setDescription("Downloading submission file...");

            // Allow download over mobile and WiFi
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            // Start download
            long downloadId = downloadManager.enqueue(request);

            Toast.makeText(context, "Download started: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Cannot download file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showGradeDialog(Submission submission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Grade Submission");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_grade_submission, null);
        EditText etGrade = view.findViewById(R.id.etGrade);
        EditText etFeedback = view.findViewById(R.id.etFeedback);

        // Pre-fill with existing data if available
        if (submission.getGrade() != null && !submission.getGrade().isEmpty()) {
            etGrade.setText(submission.getGrade());
        }

        if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
            etFeedback.setText(submission.getFeedback());
        }

        builder.setView(view);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String grade = etGrade.getText().toString().trim();
            String feedback = etFeedback.getText().toString().trim();

            if (TextUtils.isEmpty(grade)) {
                Toast.makeText(context, "Please enter a grade", Toast.LENGTH_SHORT).show();
                return;
            }

            submitGrade(submission, grade, feedback);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void submitGrade(Submission submission, String grade, String feedback) {
        // Check if this is an update (existing grade) or new grade
        boolean isUpdate = submission.getStatus() != null && submission.getStatus().equals("graded");

        // Create request with the correct field names to match JavaScript
        GradeRequest request = new GradeRequest(
                submission.getGradeId(), // existing grade_id for updates, null for new grades
                submission.getId(), // assignment_submission_id
                grade,
                feedback
        );

        // Get the token from SessionManager
        String token = sessionManager.getToken();

        // Add "Bearer " prefix if it's not already there
        if (token != null && !token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        // Use different API calls based on whether it's an update or new grade
        Call<MessageResponse> call;
        if (isUpdate) {
            // Use PUT for updates
            call = RetrofitClient.getClient().create(com.educonnect.api.ApiService.class)
                    .updateGrade(token, request);
        } else {
            // Use POST for new grades
            call = RetrofitClient.getClient().create(com.educonnect.api.ApiService.class)
                    .createGrade(token, request);
        }

        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if ("success".equals(messageResponse.getStatus())) {
                        Toast.makeText(context, "Grade saved successfully", Toast.LENGTH_SHORT).show();

                        // Update the submission in the list
                        submission.setGrade(grade);
                        submission.setFeedback(feedback);
                        submission.setStatus("graded");

                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Error: " + messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to save grade", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class SubmissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvSubmissionDate, tvCurrentGrade, tvFeedback;
        Button btnViewFile, btnGrade;

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvSubmissionDate = itemView.findViewById(R.id.tvSubmissionDate);
            tvCurrentGrade = itemView.findViewById(R.id.tvCurrentGrade);
            tvFeedback = itemView.findViewById(R.id.tvFeedback);
            btnViewFile = itemView.findViewById(R.id.btnViewFile);
            btnGrade = itemView.findViewById(R.id.btnGrade);
        }
    }
}

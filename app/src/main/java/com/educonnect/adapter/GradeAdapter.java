package com.educonnect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.R;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.GradeResponse.Grade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {
    private List<Grade> grades = new ArrayList<>();
    private Context context;
    private FileClickListener fileClickListener;

    public interface FileClickListener {
        void onFileClick(String fileUrl);
    }

    public GradeAdapter(Context context, FileClickListener fileClickListener) {
        this.context = context;
        this.fileClickListener = fileClickListener;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
        notifyDataSetChanged();
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

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = grades.get(position);

        holder.tvAssignmentTitle.setText(grade.getAssignmentTitle());

        // Set grade
        if (grade.isGraded()) {
            holder.tvGrade.setText(grade.getGrade());
        } else {
            holder.tvGrade.setText("Not graded yet");
        }

        // Set feedback
        if (grade.getFeedback() != null && !grade.getFeedback().isEmpty()) {
            holder.tvFeedback.setText(grade.getFeedback());
        } else {
            holder.tvFeedback.setText("No feedback provided");
        }

        // Format submission date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US);
            Date date = inputFormat.parse(grade.getSubmissionCreatedAt());
            holder.tvSubmittedAt.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvSubmittedAt.setText(grade.getSubmissionCreatedAt());
        }

        // Format graded date if available
        if (grade.getGradeCreatedAt() != null && !grade.getGradeCreatedAt().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US);
                Date date = inputFormat.parse(grade.getGradeCreatedAt());
                holder.tvGradedAt.setText(outputFormat.format(date));
                holder.layoutGradedAt.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                holder.tvGradedAt.setText(grade.getGradeCreatedAt());
                holder.layoutGradedAt.setVisibility(View.VISIBLE);
            }
        } else {
            holder.layoutGradedAt.setVisibility(View.GONE);
        }

        // Handle submission file button
        if (grade.getSubmissionFile() != null && !grade.getSubmissionFile().isEmpty()) {
            holder.btnViewSubmission.setVisibility(View.VISIBLE);
            holder.btnViewSubmission.setOnClickListener(v -> {
                if (fileClickListener != null) {
                    String downloadUrl = constructDownloadUrl(grade.getSubmissionFile());
                    fileClickListener.onFileClick(downloadUrl);
                }
            });
        } else {
            holder.btnViewSubmission.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return grades.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvAssignmentTitle;
        TextView tvGrade;
        TextView tvFeedback;
        TextView tvSubmittedAt;
        TextView tvGradedAt;
        LinearLayout layoutGradedAt;
        Button btnViewSubmission;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAssignmentTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvFeedback = itemView.findViewById(R.id.tvFeedback);
            tvSubmittedAt = itemView.findViewById(R.id.tvSubmittedAt);
            tvGradedAt = itemView.findViewById(R.id.tvGradedAt);
            layoutGradedAt = itemView.findViewById(R.id.layoutGradedAt);
            btnViewSubmission = itemView.findViewById(R.id.btnViewSubmission);
        }
    }
}

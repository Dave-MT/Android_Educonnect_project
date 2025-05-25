package com.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.R;
import com.educonnect.SubmitAssignmentActivity;
import com.educonnect.StudentAssignmentsActivity;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.AssignmentResponse.Assignment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentAssignmentAdapter extends RecyclerView.Adapter<StudentAssignmentAdapter.AssignmentViewHolder> {
    private List<Assignment> assignments = new ArrayList<>();
    private Context context;

    public StudentAssignmentAdapter(List<Assignment> assignments, Context context) {
        this.assignments = assignments;
        this.context = context;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);

        holder.tvTitle.setText(assignment.getTitle());
        holder.tvDescription.setText(assignment.getDescription());

        // Format deadline
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US);
            Date date = inputFormat.parse(assignment.getDueDate());
            holder.tvDeadline.setText("Deadline: " + outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvDeadline.setText("Deadline: " + assignment.getDueDate());
        }

        // Handle file download button
        if (assignment.getFile() != null && !assignment.getFile().isEmpty()) {
            holder.btnDownload.setVisibility(View.VISIBLE);
            holder.btnDownload.setOnClickListener(v -> {
                String fileUrl = constructDownloadUrl(assignment.getFile());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                context.startActivity(browserIntent);
            });
        } else {
            holder.btnDownload.setVisibility(View.GONE);
        }

        // Handle submission status - check both isSubmitted boolean and status string
        if (assignment.isSubmitted() || "submitted".equals(assignment.getStatus())) {
            holder.btnSubmit.setVisibility(View.GONE);
            holder.tvSubmitted.setVisibility(View.VISIBLE);
            holder.tvSubmitted.setText("Submitted");
            holder.tvSubmitted.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.btnSubmit.setVisibility(View.VISIBLE);
            holder.tvSubmitted.setVisibility(View.GONE);

            holder.btnSubmit.setOnClickListener(v -> {
                Intent intent = new Intent(context, SubmitAssignmentActivity.class);
                intent.putExtra("ASSIGNMENT_ID", assignment.getId());
                intent.putExtra("ASSIGNMENT_TITLE", assignment.getTitle());

                // Use activity result launcher if context is StudentAssignmentsActivity
                if (context instanceof StudentAssignmentsActivity) {
                    ((StudentAssignmentsActivity) context).getSubmitAssignmentLauncher().launch(intent);
                } else {
                    context.startActivity(intent);
                }
            });
        }
    }

    private String constructDownloadUrl(String filePath) {
        // Get base URL and remove /api/ part for file downloads
        String baseUrl = RetrofitClient.getBaseUrl().replace("/educonnect/api", "");

        // Clean the file path
        String cleanPath = filePath;

        // Remove any leading slashes
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        // Extract just the filename if it contains uploads/
        if (cleanPath.contains("uploads/")) {
            int uploadsIndex = cleanPath.indexOf("uploads/");
            cleanPath = cleanPath.substring(uploadsIndex);
        } else {
            // If it's just a filename, prepend uploads/
            cleanPath = "uploads/" + cleanPath;
        }

        // Construct final URL: baseUrl/educonnect/uploads/filename
        return baseUrl + "/educonnect/" + cleanPath;
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDeadline;
        Button btnDownload;
        Button btnSubmit;
        TextView tvSubmitted;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            tvSubmitted = itemView.findViewById(R.id.tvSubmitted);
        }
    }
}

package com.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.EditAssignmentActivity;
import com.educonnect.GradeSubmissionsActivity;
import com.educonnect.R;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.model.MessageResponse;
import com.educonnect.utils.DateUtils;
import com.educonnect.utils.SessionManager;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAssignmentAdapter extends RecyclerView.Adapter<TeacherAssignmentAdapter.AssignmentViewHolder> {
    private List<Assignment> assignmentList;
    private Context context;
    private SessionManager sessionManager;

    public TeacherAssignmentAdapter(List<Assignment> assignmentList, Context context) {
        this.assignmentList = assignmentList;
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignmentList.get(position);

        holder.tvTitle.setText(assignment.getTitle());
        holder.tvDescription.setText(assignment.getDescription());
        holder.tvClassName.setText("Class: " + assignment.getClassName());

        // Format and display the deadline
        String formattedDeadline = DateUtils.formatDateTime(assignment.getDueDate());
        holder.tvDeadline.setText("Due: " + formattedDeadline);

        // Set click listeners for buttons
        holder.btnGrade.setOnClickListener(v -> {
            Intent intent = new Intent(context, GradeSubmissionsActivity.class);
            intent.putExtra("assignment_id", assignment.getId());
            intent.putExtra("assignment_title", assignment.getTitle());
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAssignmentActivity.class);
            intent.putExtra("assignment_id", assignment.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            deleteAssignment(assignment.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    private void deleteAssignment(int assignmentId, int position) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        // Create request body with assignment ID
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", assignmentId);

        Call<MessageResponse> call = apiService.deleteAssignment(token, requestBody);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getStatus().equals("success")) {
                        // Remove the item from the list and notify adapter
                        assignmentList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, assignmentList.size());
                        Toast.makeText(context, "Assignment deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error: " + messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to delete assignment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvClassName, tvDeadline;
        Button btnGrade, btnEdit, btnDelete;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            btnGrade = itemView.findViewById(R.id.btnGrade);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

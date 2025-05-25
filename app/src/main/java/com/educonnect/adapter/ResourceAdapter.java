package com.educonnect.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.EditResourceActivity;
import com.educonnect.R;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.ResourceResponse;
import com.educonnect.utils.DateUtils;
import com.educonnect.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ResourceAdapter";
    private static final int VIEW_TYPE_TEACHER = 1;
    private static final int VIEW_TYPE_STUDENT = 2;

    private Context context;
    private List<ResourceResponse.Resource> resources;
    private FileClickListener fileClickListener;
    private OnResourceDeletedListener deleteListener;
    private SessionManager sessionManager;
    private boolean isTeacher;

    public interface FileClickListener {
        void onFileClick(String url);
    }

    public interface OnResourceDeletedListener {
        void onResourceDeleted();
    }

    public ResourceAdapter(Context context, FileClickListener fileClickListener, boolean isTeacher) {
        this.context = context;
        this.fileClickListener = fileClickListener;
        this.sessionManager = new SessionManager(context);
        this.isTeacher = isTeacher;
    }

    public void setResources(List<ResourceResponse.Resource> resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    public void setOnResourceDeletedListener(OnResourceDeletedListener listener) {
        this.deleteListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isTeacher ? VIEW_TYPE_TEACHER : VIEW_TYPE_STUDENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEACHER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_resource, parent, false);
            return new TeacherResourceViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_resource_student, parent, false);
            return new StudentResourceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ResourceResponse.Resource resource = resources.get(position);

        if (holder instanceof TeacherResourceViewHolder) {
            bindTeacherView((TeacherResourceViewHolder) holder, resource);
        } else if (holder instanceof StudentResourceViewHolder) {
            bindStudentView((StudentResourceViewHolder) holder, resource);
        }
    }

    private void bindTeacherView(TeacherResourceViewHolder holder, ResourceResponse.Resource resource) {
        holder.tvTitle.setText(resource.getTitle());
        holder.tvClassName.setText("Class: " + resource.getClassName());

        // Format and display date
        if (resource.getCreatedAt() != null) {
            String formattedDate = DateUtils.formatDate(resource.getCreatedAt());
            holder.tvDate.setText("Created: " + formattedDate);
        }

        // Download button
        holder.btnDownload.setOnClickListener(v -> handleDownload(resource));

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditResourceActivity.class);
            intent.putExtra("resource_id", resource.getId());
            context.startActivity(intent);
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(resource));
    }

    private void bindStudentView(StudentResourceViewHolder holder, ResourceResponse.Resource resource) {
        holder.tvTitle.setText(resource.getTitle());
        holder.tvClassName.setText("Class: " + resource.getClassName());

        // Format and display date
        if (resource.getCreatedAt() != null) {
            String formattedDate = DateUtils.formatDate(resource.getCreatedAt());
            holder.tvDate.setText("Created: " + formattedDate);
        }

        // Download button
        holder.btnDownload.setOnClickListener(v -> handleDownload(resource));
    }

    private void handleDownload(ResourceResponse.Resource resource) {
        String resourceUrl = resource.getResourceUrl();
        if (resourceUrl != null && !resourceUrl.isEmpty()) {
            if (fileClickListener != null) {
                fileClickListener.onFileClick(resourceUrl);
            }
        } else {
            Toast.makeText(context, "No file available for download", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(ResourceResponse.Resource resource) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Resource")
                .setMessage("Are you sure you want to delete \"" + resource.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteResource(resource))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteResource(ResourceResponse.Resource resource) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        // Create request body with resource ID
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("id", resource.getId());

        Call<MessageResponse> call = apiService.deleteResource(token, requestBody);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getStatus().equals("success")) {
                        Toast.makeText(context, "Resource deleted successfully", Toast.LENGTH_SHORT).show();
                        if (deleteListener != null) {
                            deleteListener.onResourceDeleted();
                        }
                    } else {
                        Toast.makeText(context, "Error: " + messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Failed to delete resource", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e(TAG, "Error deleting resource", t);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return resources != null ? resources.size() : 0;
    }

    // Teacher ViewHolder with all buttons
    static class TeacherResourceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvClassName, tvDate;
        Button btnDownload, btnEdit, btnDelete;

        public TeacherResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Student ViewHolder with only download button
    static class StudentResourceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvClassName, tvDate;
        Button btnDownload;

        public StudentResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}

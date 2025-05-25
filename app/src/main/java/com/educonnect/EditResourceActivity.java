package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Class;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.SingleResourceResponse;
import com.educonnect.model.TeacherResourceRequest;
import com.educonnect.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditResourceActivity extends BaseActivity {
    private static final String TAG = "EditResourceActivity";

    private TextInputEditText etTitle;
    private Spinner spinnerClass;
    private TextView tvCurrentFile;
    private Button btnCancel, btnUpdate;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private List<Class> classList = new ArrayList<>();
    private ArrayAdapter<String> classAdapter;
    private int resourceId;
    private SingleResourceResponse.ResourceData currentResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_resource);

        sessionManager = new SessionManager(this);

        // Get resource ID from intent
        resourceId = getIntent().getIntExtra("resource_id", -1);
        if (resourceId == -1) {
            Toast.makeText(this, "Invalid resource ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadClasses();
        loadResource();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        spinnerClass = findViewById(R.id.spinnerClass);
        tvCurrentFile = findViewById(R.id.tvCurrentFile);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        // Setup class spinner
        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateResource());
    }

    private void loadClasses() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<ClassResponse> call = apiService.getClasses(token);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClassResponse classResponse = response.body();
                    if (classResponse.getStatus().equals("success") && classResponse.getData() != null) {
                        classList.clear();
                        // Convert ClassResponse.ClassData to Class objects
                        for (ClassResponse.ClassData classData : classResponse.getData()) {
                            Class cls = new Class();
                            cls.setId(classData.getId());
                            cls.setName(classData.getName());
                            cls.setDescription(classData.getDescription());
                            classList.add(cls);
                        }

                        List<String> classNames = new ArrayList<>();
                        for (Class cls : classList) {
                            classNames.add(cls.getName());
                        }

                        classAdapter.clear();
                        classAdapter.addAll(classNames);
                        classAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                Log.e(TAG, "Error loading classes", t);
                Toast.makeText(EditResourceActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadResource() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<SingleResourceResponse> call = apiService.getResourceById(token, resourceId);
        call.enqueue(new Callback<SingleResourceResponse>() {
            @Override
            public void onResponse(Call<SingleResourceResponse> call, Response<SingleResourceResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    SingleResourceResponse resourceResponse = response.body();
                    if (resourceResponse.getStatus().equals("success") && resourceResponse.getData() != null) {
                        currentResource = resourceResponse.getData();
                        populateFields();
                    } else {
                        Toast.makeText(EditResourceActivity.this, "Resource not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EditResourceActivity.this, "Failed to load resource", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SingleResourceResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading resource", t);
                Toast.makeText(EditResourceActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentResource != null) {
            etTitle.setText(currentResource.getTitle());

            // Set class selection
            for (int i = 0; i < classList.size(); i++) {
                if (classList.get(i).getId() == currentResource.getClassId()) {
                    spinnerClass.setSelection(i);
                    break;
                }
            }

            // Show current file info
            if (currentResource.getFileUrl() != null && !currentResource.getFileUrl().isEmpty()) {
                String fileName = currentResource.getFileUrl().substring(currentResource.getFileUrl().lastIndexOf("/") + 1);
                tvCurrentFile.setText("Current file: " + fileName);
                tvCurrentFile.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateResource() {
        String title = etTitle.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (spinnerClass.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        Class selectedClass = classList.get(spinnerClass.getSelectedItemPosition());
        int userId = sessionManager.getUserId();

        TeacherResourceRequest request = new TeacherResourceRequest();
        request.setId(resourceId);
        request.setTitle(title);
        request.setClassId(selectedClass.getId());
        request.setUploadedBy(userId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<MessageResponse> call = apiService.updateResource(token, request);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getStatus().equals("success")) {
                        Toast.makeText(EditResourceActivity.this, "Resource updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditResourceActivity.this, "Error: " + messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditResourceActivity.this, "Failed to update resource", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error updating resource", t);
                Toast.makeText(EditResourceActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnUpdate.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnUpdate.setEnabled(true);
        }
    }
}

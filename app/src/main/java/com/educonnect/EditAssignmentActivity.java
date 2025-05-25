package com.educonnect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.Assignment;
import com.educonnect.model.Class;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.MessageResponse;
import com.educonnect.utils.FileUtils;
import com.educonnect.utils.SessionManager;
import com.educonnect.model.AssignmentResponse;

import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAssignmentActivity extends BaseActivity {
    private static final String TAG = "EditAssignmentActivity";
    private static final int PICK_FILE_REQUEST = 1;

    private EditText etTitle, etDescription;
    private Spinner spinnerClass;
    private TextView tvDeadline, tvSelectedFile;
    private Button btnSelectDate, btnSelectFile, btnUpdate;
    private ProgressBar progressBar;

    private List<Class> classList = new ArrayList<>();
    private Map<String, Integer> classNameToIdMap = new HashMap<>();
    private int assignmentId;
    private String selectedFilePath;
    private Calendar deadlineCalendar = Calendar.getInstance();
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assignment);

        // Set up action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit Assignment");
        }

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spinnerClass = findViewById(R.id.spinnerClass);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Get assignment ID from intent
        assignmentId = getIntent().getIntExtra("assignment_id", -1);
        if (assignmentId == -1) {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading assignment with ID: " + assignmentId);

        // Set up date selection
        btnSelectDate.setOnClickListener(v -> showDateTimePicker());

        // Set up file selection
        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
        });

        // Set up update button
        btnUpdate.setOnClickListener(v -> updateAssignment());

        // Load classes and assignment details
        loadClasses();
    }

    private void loadClasses() {
        showProgress(true);
        String token = "Bearer " + sessionManager.getToken();

        Call<ClassResponse> call = apiService.getClasses(token);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Fix the type mismatch by converting ClassData to Class
                    classList = convertClassDataToClass(response.body().getData());
                    setupClassSpinner();
                    loadAssignmentDetails();
                } else {
                    showProgress(false);
                    Toast.makeText(EditAssignmentActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                showProgress(false);
                Toast.makeText(EditAssignmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a conversion method to convert ClassData to Class
    private List<Class> convertClassDataToClass(List<ClassResponse.ClassData> classDataList) {
        List<Class> result = new ArrayList<>();
        if (classDataList != null) {
            for (ClassResponse.ClassData data : classDataList) {
                Class classItem = new Class();
                classItem.setId(data.getId());
                // Use getClassName() instead of getName()
                classItem.setName(data.getClassName());
                classItem.setDescription(data.getDescription());
                // Set other fields as needed
                result.add(classItem);
            }
        }
        return result;
    }

    private void setupClassSpinner() {
        List<String> classNames = new ArrayList<>();
        classNameToIdMap.clear();

        for (Class classItem : classList) {
            String className = classItem.getName();
            classNames.add(className);
            classNameToIdMap.put(className, classItem.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, classNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);
    }

    private void loadAssignmentDetails() {
        String token = "Bearer " + sessionManager.getToken();

        // Create a custom response model for single assignment
        Call<SingleAssignmentResponse> call = apiService.getSingleAssignment(token, assignmentId);
        call.enqueue(new Callback<SingleAssignmentResponse>() {
            @Override
            public void onResponse(Call<SingleAssignmentResponse> call, Response<SingleAssignmentResponse> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    SingleAssignmentResponse assignmentResponse = response.body();
                    if (assignmentResponse.getStatus().equals("success") && assignmentResponse.getData() != null) {
                        populateAssignmentDetails(assignmentResponse.getData());
                    } else {
                        Toast.makeText(EditAssignmentActivity.this, "Assignment not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EditAssignmentActivity.this, "Failed to load assignment details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SingleAssignmentResponse> call, Throwable t) {
                showProgress(false);
                Log.e(TAG, "Network error loading assignment", t);
                Toast.makeText(EditAssignmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateAssignmentDetails(Assignment assignment) {
        Log.d(TAG, "Populating assignment details: " + assignment.getTitle());

        etTitle.setText(assignment.getTitle());
        etDescription.setText(assignment.getDescription());

        // Set class spinner selection based on class_id
        int classId = assignment.getClassId();
        for (int i = 0; i < classList.size(); i++) {
            if (classList.get(i).getId() == classId) {
                spinnerClass.setSelection(i);
                break;
            }
        }

        // Set deadline with null check
        String deadlineStr = assignment.getDeadline();
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadlineStr);
                if (deadlineDate != null) {
                    deadlineCalendar.setTime(deadlineDate);
                    updateDeadlineDisplay();
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing deadline date: " + deadlineStr, e);
                // Try alternative format
                try {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date deadlineDate = sdf2.parse(deadlineStr);
                    if (deadlineDate != null) {
                        deadlineCalendar.setTime(deadlineDate);
                        updateDeadlineDisplay();
                    }
                } catch (ParseException e2) {
                    Log.e(TAG, "Error parsing deadline date with alternative format: " + deadlineStr, e2);
                }
            }
        }

        // Set file name if available
        String fileUrl = assignment.getFile();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            tvSelectedFile.setText("📄 " + fileName);
            tvSelectedFile.setVisibility(View.VISIBLE);
        } else {
            tvSelectedFile.setText("📄 No file selected");
            tvSelectedFile.setVisibility(View.VISIBLE);
        }
    }

    private void showDateTimePicker() {
        // Date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    deadlineCalendar.set(Calendar.YEAR, year);
                    deadlineCalendar.set(Calendar.MONTH, month);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                deadlineCalendar.set(Calendar.MINUTE, minute);
                                updateDeadlineDisplay();
                            },
                            deadlineCalendar.get(Calendar.HOUR_OF_DAY),
                            deadlineCalendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                deadlineCalendar.get(Calendar.YEAR),
                deadlineCalendar.get(Calendar.MONTH),
                deadlineCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDeadlineDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvDeadline.setText(sdf.format(deadlineCalendar.getTime()));
    }

    private void updateAssignment() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String selectedClass = spinnerClass.getSelectedItem() != null ?
                spinnerClass.getSelectedItem().toString() : "";

        // Validate inputs
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        if (selectedClass.isEmpty() || !classNameToIdMap.containsKey(selectedClass)) {
            Toast.makeText(this, "Please select a valid class", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tvDeadline.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        String token = "Bearer " + sessionManager.getToken();
        int classId = classNameToIdMap.get(selectedClass);
        int teacherId = sessionManager.getUserId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String deadline = sdf.format(deadlineCalendar.getTime());

        try {
            // Create JSON object exactly like the JavaScript example
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", assignmentId);
            jsonData.put("title", title);
            jsonData.put("description", description);
            jsonData.put("teacher_id", teacherId);
            jsonData.put("class_id", classId);
            jsonData.put("deadline", deadline);

            String jsonString = jsonData.toString();
            Log.d(TAG, "Sending JSON: " + jsonString);

            // Create RequestBody with JSON content type
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonString
            );

            // Make API call with JSON body
            Call<MessageResponse> call = apiService.updateAssignmentJson(token, requestBody);
            call.enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    showProgress(false);
                    if (response.isSuccessful() && response.body() != null) {
                        MessageResponse messageResponse = response.body();
                        Log.d(TAG, "Update response: " + messageResponse.getStatus() + " - " + messageResponse.getMessage());
                        if (messageResponse.getStatus().equals("success")) {
                            Toast.makeText(EditAssignmentActivity.this, "Assignment updated successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditAssignmentActivity.this, "Error: " + messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Update failed with response code: " + response.code());
                        Toast.makeText(EditAssignmentActivity.this, "Failed to update assignment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    showProgress(false);
                    Log.e(TAG, "Update network error", t);
                    Toast.makeText(EditAssignmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Log.e(TAG, "Error creating JSON", e);
            Toast.makeText(this, "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                try {
                    selectedFilePath = FileUtils.getPath(this, fileUri);
                    String fileName = new File(selectedFilePath).getName();
                    tvSelectedFile.setText(fileName);
                    tvSelectedFile.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting file path", e);
                    Toast.makeText(this, "Error selecting file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class for single assignment response
    public static class SingleAssignmentResponse {
        private String status;
        private Assignment data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Assignment getData() {
            return data;
        }

        public void setData(Assignment data) {
            this.data = data;
        }
    }
}

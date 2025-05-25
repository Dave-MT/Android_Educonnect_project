package com.educonnect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.TeacherAssignmentRequest;
import com.educonnect.utils.DateUtils;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAssignmentActivity extends BaseActivity {
    private static final String TAG = "CreateAssignmentActivity";

    private EditText etTitle, etDescription, etDueDate;
    private Spinner spinnerClass;
    private Button btnSubmit, btnCancel, btnSelectFile;
    private TextView tvSelectedFile;
    private Calendar calendar;
    private SessionManager sessionManager;
    private List<ClassResponse.ClassData> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private List<Integer> classIds = new ArrayList<>();
    private String selectedFileBase64 = null;
    private String selectedFileName = null;

    // File picker launcher
    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        handleSelectedFile(fileUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_assignment);

        sessionManager = new SessionManager(this);

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Set up date picker for due date
        etDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Set up file selection
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        // Initialize spinner with a placeholder item to prevent null pointer exception
        classNames.add("Loading classes...");
        classIds.add(-1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                CreateAssignmentActivity.this,
                android.R.layout.simple_spinner_item,
                classNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);

        // Load classes for spinner
        loadClasses();

        // Set up submit button
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitAssignment();
                }
            });
        }

        // Set up cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Assignment File"));
        } catch (Exception e) {
            Log.e(TAG, "Error opening file picker", e);
            Toast.makeText(this, "Error opening file picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSelectedFile(Uri fileUri) {
        try {
            // Get file name
            String fileName = getFileName(fileUri);
            selectedFileName = fileName;

            // Convert file to base64
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byte[] fileBytes = byteArrayOutputStream.toByteArray();
            selectedFileBase64 = Base64.encodeToString(fileBytes, Base64.DEFAULT);

            // Update UI
            tvSelectedFile.setText("Selected: " + fileName);
            tvSelectedFile.setVisibility(View.VISIBLE);

            inputStream.close();
            byteArrayOutputStream.close();

            Log.d(TAG, "File selected: " + fileName + " (Base64 length: " + selectedFileBase64.length() + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error processing selected file", e);
            Toast.makeText(this, "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            selectedFileBase64 = null;
            selectedFileName = null;
            tvSelectedFile.setVisibility(View.GONE);
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "unknown_file";
        try {
            String path = uri.getPath();
            if (path != null) {
                fileName = path.substring(path.lastIndexOf('/') + 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }
        return fileName;
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        etDueDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void loadClasses() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<ClassResponse> call = apiService.getClasses(token);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                Log.d(TAG, "Classes API Response Code: " + response.code());
                Log.d(TAG, "Classes API Response Successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ClassResponse classResponse = response.body();
                    Log.d(TAG, "Response body is not null");
                    Log.d(TAG, "Response status: " + classResponse.getStatus());
                    Log.d(TAG, "Response message: " + classResponse.getMessage());
                    Log.d(TAG, "Response data: " + (classResponse.getData() != null ? classResponse.getData().size() + " items" : "null"));

                    if (classResponse.getData() != null) {
                        for (int i = 0; i < classResponse.getData().size(); i++) {
                            ClassResponse.ClassData classData = classResponse.getData().get(i);
                            Log.d(TAG, "Class " + i + ": ID=" + classData.getId() + ", Name=" + classData.getClassName());
                        }
                    }

                    if (classResponse.getStatus().equals("success") && classResponse.getData() != null) {
                        classList.clear();
                        classNames.clear();
                        classIds.clear();

                        classList.addAll(classResponse.getData());

                        for (ClassResponse.ClassData classData : classList) {
                            if (classData != null && classData.getClassName() != null) {
                                classNames.add(classData.getClassName());
                                classIds.add(classData.getId()); // This is the class ID that will be sent to the API
                                Log.d(TAG, "Added class: " + classData.getClassName() + " with ID: " + classData.getId());
                            }
                        }

                        // If no classes were found, add a placeholder
                        if (classNames.isEmpty()) {
                            Log.w(TAG, "No classes found in response data");
                            classNames.add("No classes available");
                            classIds.add(-1);
                        } else {
                            Log.d(TAG, "Successfully loaded " + classNames.size() + " classes");
                        }

                        // Set up spinner adapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                CreateAssignmentActivity.this,
                                android.R.layout.simple_spinner_item,
                                classNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClass.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Response status is not success or data is null");
                        Log.e(TAG, "Status: " + classResponse.getStatus());
                        Log.e(TAG, "Data is null: " + (classResponse.getData() == null));

                        // Handle error response
                        classNames.clear();
                        classIds.clear();
                        classNames.add("Error loading classes");
                        classIds.add(-1);
                        updateSpinnerAdapter();

                        Toast.makeText(CreateAssignmentActivity.this,
                                "Error loading classes: " + classResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful or body is null");
                    Log.e(TAG, "Response code: " + response.code());
                    Log.e(TAG, "Response body is null: " + (response.body() == null));

                    // Handle error response
                    classNames.clear();
                    classIds.clear();
                    classNames.add("Error loading classes");
                    classIds.add(-1);
                    updateSpinnerAdapter();

                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading classes: " + errorBody);
                        Toast.makeText(CreateAssignmentActivity.this,
                                "Failed to load classes: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading classes: " + t.getMessage(), t);
                Log.e(TAG, "Request URL: " + call.request().url());

                // Handle network error
                classNames.clear();
                classIds.clear();
                classNames.add("Network error");
                classIds.add(-1);
                updateSpinnerAdapter();

                Toast.makeText(CreateAssignmentActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                CreateAssignmentActivity.this,
                android.R.layout.simple_spinner_item,
                classNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);
    }

    private void submitAssignment() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String deadline = etDueDate.getText().toString().trim(); // This matches your JS 'deadline' field

        // Validate inputs
        if (title.isEmpty() || description.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerClass.getSelectedItemPosition() == -1 || classIds.isEmpty()) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerClass.getSelectedItemPosition();
        if (selectedPosition >= classIds.size()) {
            Toast.makeText(this, "Invalid class selection", Toast.LENGTH_SHORT).show();
            return;
        }

        int classId = classIds.get(selectedPosition); // This is the class_id that matches your JS
        if (classId == -1) {
            Toast.makeText(this, "Please select a valid class", Toast.LENGTH_SHORT).show();
            return;
        }

        // File is optional - if no file selected, send empty string instead of null
        String fileBase64 = selectedFileBase64 != null ? selectedFileBase64 : "";

        int teacherId = JwtUtils.extractUserId(sessionManager.getToken()); // This matches your JS 'teacher_id: userId'

        // Create request object matching your JavaScript implementation exactly
        TeacherAssignmentRequest request = new TeacherAssignmentRequest();
        request.setTitle(title);                    // matches JS: title: title
        request.setDescription(description);        // matches JS: description: description
        request.setClassId(classId);               // matches JS: class_id: classId
        request.setDeadline(deadline);             // matches JS: deadline: deadline
        request.setFileBase64(fileBase64);         // matches JS: file: fileBase64
        request.setTeacherId(teacherId);           // matches JS: teacher_id: userId

        // Log the request data for debugging
        Log.d(TAG, "Creating assignment with data:");
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "Class ID: " + classId);
        Log.d(TAG, "Deadline: " + deadline);
        Log.d(TAG, "Teacher ID: " + teacherId);
        Log.d(TAG, "File selected: " + (selectedFileBase64 != null ? "Yes (" + selectedFileName + ")" : "No"));
        Log.d(TAG, "File base64 length: " + (selectedFileBase64 != null ? selectedFileBase64.length() : 0));

        // Send API request to /api/assignment.php (POST)
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<MessageResponse> call = apiService.createAssignment(token, request);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                Log.d(TAG, "Assignment creation response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Assignment created successfully");
                    Toast.makeText(CreateAssignmentActivity.this,
                            "Assignment created successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error creating assignment: " + errorBody);
                        Log.e(TAG, "Response code: " + response.code());
                        Toast.makeText(CreateAssignmentActivity.this,
                                "Failed to create assignment: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e(TAG, "Network error creating assignment", t);
                Toast.makeText(CreateAssignmentActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

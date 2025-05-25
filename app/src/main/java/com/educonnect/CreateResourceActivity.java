package com.educonnect;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.TeacherResourceRequest;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateResourceActivity extends BaseActivity {
    private static final String TAG = "CreateResourceActivity";

    private EditText etTitle;
    private TextView tvSelectedFile;
    private Button btnSelectFile;
    private Uri selectedFileUri;
    private static final int PICK_FILE_REQUEST = 1;
    private Spinner spinnerClass;
    private Button btnSubmit, btnCancel;
    private SessionManager sessionManager;
    private List<ClassResponse.ClassData> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private List<Integer> classIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_resource);

        sessionManager = new SessionManager(this);

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize spinner with a placeholder item to prevent null pointer exception
        classNames.add("Loading classes...");
        classIds.add(-1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                CreateResourceActivity.this,
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
                    submitResource();
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

        // Set up file picker button
        if (btnSelectFile != null) {
            btnSelectFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFilePicker();
                }
            });
        }
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
                        classNames.clear();
                        classIds.clear();

                        classList.addAll(classResponse.getData());

                        for (ClassResponse.ClassData classData : classList) {
                            if (classData != null && classData.getClassName() != null) {
                                classNames.add(classData.getClassName());
                                classIds.add(classData.getId());
                            }
                        }

                        // If no classes were found, add a placeholder
                        if (classNames.isEmpty()) {
                            classNames.add("No classes available");
                            classIds.add(-1);
                        }

                        // Set up spinner adapter
                        updateSpinnerAdapter();
                    } else {
                        // Handle error response
                        classNames.clear();
                        classIds.clear();
                        classNames.add("Error loading classes");
                        classIds.add(-1);
                        updateSpinnerAdapter();

                        Toast.makeText(CreateResourceActivity.this,
                                "Error loading classes: " + classResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
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
                        Toast.makeText(CreateResourceActivity.this,
                                "Failed to load classes: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                // Handle network error
                classNames.clear();
                classIds.clear();
                classNames.add("Network error");
                classIds.add(-1);
                updateSpinnerAdapter();

                Log.e(TAG, "Network error loading classes", t);
                Toast.makeText(CreateResourceActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                CreateResourceActivity.this,
                android.R.layout.simple_spinner_item,
                classNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String fileName = getFileName(selectedFileUri);
                tvSelectedFile.setText(fileName != null ? fileName : "File selected");
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void submitResource() {
        String title = etTitle.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
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

        int classId = classIds.get(selectedPosition);
        if (classId == -1) {
            Toast.makeText(this, "Please select a valid class", Toast.LENGTH_SHORT).show();
            return;
        }

        int teacherId = JwtUtils.extractUserId(sessionManager.getToken());

        try {
            // Create multipart request
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            String token = "Bearer " + sessionManager.getToken();

            // Create request body parts
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody classIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(classId));
            RequestBody uploadedByBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(teacherId));

            // Create file part
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();

            String fileName = getFileName(selectedFileUri);
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), fileBytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, fileBody);

            Call<MessageResponse> call = apiService.createResourceWithFile(token, titleBody, classIdBody, uploadedByBody, filePart);
            call.enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CreateResourceActivity.this,
                                "Resource created successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "Error creating resource: " + errorBody);
                            Toast.makeText(CreateResourceActivity.this,
                                    "Failed to create resource: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    Log.e(TAG, "Network error creating resource", t);
                    Toast.makeText(CreateResourceActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing file upload", e);
            Toast.makeText(this, "Error preparing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

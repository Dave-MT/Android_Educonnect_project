package com.educonnect;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.SubmissionResponse;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitAssignmentActivity extends AppCompatActivity {
    private static final String TAG = "SubmitAssignmentActivity";
    private static final int REQUEST_CODE_PICK_FILE = 101;

    private TextView tvAssignmentTitle;
    private EditText etComment;
    private Button btnChooseFile;
    private TextView tvFileName;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;

    private SessionManager sessionManager;
    private int assignmentId;
    private Uri selectedFileUri;
    private String selectedFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Submit Assignment");

        // Initialize views
        tvAssignmentTitle = findViewById(R.id.tvAssignmentTitle);
        etComment = findViewById(R.id.etComment);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        tvFileName = findViewById(R.id.tvFileName);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        // Get assignment ID and title from intent
        assignmentId = getIntent().getIntExtra("ASSIGNMENT_ID", -1);
        String assignmentTitle = getIntent().getStringExtra("ASSIGNMENT_TITLE");

        if (assignmentId == -1 || assignmentTitle == null) {
            Toast.makeText(this, "Invalid assignment information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set assignment title
        tvAssignmentTitle.setText(assignmentTitle);

        // Set up choose file button
        btnChooseFile.setOnClickListener(v -> chooseFile());

        // Set up submit button
        btnSubmit.setOnClickListener(v -> submitAssignment());
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_CODE_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedFileUri = data.getData();
                selectedFileName = getFileName(selectedFileUri);
                tvFileName.setText(selectedFileName);
                tvFileName.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
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

    private File createTempFileFromUri(Uri uri, String fileName) throws IOException {
        File tempFile = new File(getCacheDir(), fileName);
        tempFile.createNewFile();

        try (OutputStream outputStream = new FileOutputStream(tempFile);
             InputStream inputStream = getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return tempFile;
        }
    }

    private void submitAssignment() {
        String comment = etComment.getText().toString().trim();

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        clearError();

        try {
            // Get user ID from token
            int userId = JwtUtils.getUserIdFromToken(sessionManager.getToken());
            if (userId == -1) {
                showError("Invalid user information. Please log in again.");
                return;
            }

            // Create temp file from URI
            File file = createTempFileFromUri(selectedFileUri, selectedFileName);

            // Create multipart request
            RequestBody assignmentIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(assignmentId));
            RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
            RequestBody commentPart = RequestBody.create(MediaType.parse("text/plain"), comment);

            RequestBody fileRequestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedFileUri)), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", selectedFileName, fileRequestBody);

            // Make API call
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            String token = "Bearer " + sessionManager.getToken();

            Call<SubmissionResponse> call = apiService.submitAssignmentWithFile(token, assignmentIdPart, userIdPart, commentPart, filePart);
            call.enqueue(new Callback<SubmissionResponse>() {
                @Override
                public void onResponse(Call<SubmissionResponse> call, Response<SubmissionResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        SubmissionResponse submissionResponse = response.body();

                        if (submissionResponse.getStatus().equals("success")) {
                            Toast.makeText(SubmitAssignmentActivity.this,
                                    "Assignment submitted successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            showError(submissionResponse.getMessage());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "Error submitting assignment: " + errorBody);
                            showError("Failed to submit assignment: " + response.code());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                            showError("Failed to submit assignment");
                        }
                    }
                }

                @Override
                public void onFailure(Call<SubmissionResponse> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Network error submitting assignment", t);
                    showError("Network error: " + t.getMessage());
                }
            });

        } catch (IOException e) {
            showLoading(false);
            Log.e(TAG, "Error creating temp file", e);
            showError("Error processing file: " + e.getMessage());
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnChooseFile.setEnabled(false);
            etComment.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            btnChooseFile.setEnabled(true);
            etComment.setEnabled(true);
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvErrorMessage.setVisibility(View.GONE);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SubmitAssignmentActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

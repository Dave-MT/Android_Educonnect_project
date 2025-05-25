package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.educonnect.adapter.RecyclerDiscussionAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.DiscussionMessage;
import com.educonnect.model.DiscussionResponse;
import com.educonnect.model.MessageRequest;
import com.educonnect.model.MessageResponse;
import com.educonnect.utils.JwtUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscussionActivity extends BaseActivity {
    private static final String TAG = "DiscussionActivity";

    private RecyclerView recyclerView;
    private RecyclerDiscussionAdapter adapter; // Changed to RecyclerDiscussionAdapter
    private ProgressBar progressBar;
    private TextView tvLoadingMessage;
    private TextView tvErrorMessage;
    private TextView tvNoMessages;
    private EditText etMessage;
    private Button btnSend;
    private int userId;
    private int classId;
    private Timer refreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvNoMessages = findViewById(R.id.tvNoMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Set up toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Class Discussion");
        }

        // Get user ID from token
        userId = JwtUtils.getUserIdFromToken(sessionManager.getToken());
        if (userId == -1) {
            showError("Invalid user information. Please log in again.");
            Log.e(TAG, "Failed to extract user ID from token");
            return;
        }

        Log.d(TAG, "Got user ID from token: " + userId);

        // Set up RecyclerView with null check
        recyclerView = findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerDiscussionAdapter(this, userId);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e(TAG, "RecyclerView not found in layout");
            showError("Failed to initialize discussion view");
            return;
        }

        // Get class ID for the current user
        fetchClassId();

        // Set up send button
        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start periodic refresh of messages
        startMessageRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop periodic refresh when activity is not visible
        stopMessageRefresh();
    }

    private void fetchClassId() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        // Using the user ID from the token
        Log.d(TAG, "Fetching class ID for user ID: " + userId);

        // Updated to use the correct endpoint for getting user's class ID
        Call<ClassResponse> call = apiService.getUserClassByUserId(token, userId);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ClassResponse classResponse = response.body();

                    if (classResponse.getStatus().equals("success")) {
                        classId = classResponse.getClassId();
                        Log.d(TAG, "Successfully fetched class ID: " + classId);
                        loadMessages();
                    } else {
                        showError("You are not assigned to any class.");
                        Log.e(TAG, "Error: User not assigned to any class. Response: " +
                                (classResponse.getMessage() != null ? classResponse.getMessage() : "No message"));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching class ID: " + errorBody);
                        showError("Failed to load class information: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load class information");
                    }
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching class ID", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void loadMessages() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Log.d(TAG, "Loading messages for class ID: " + classId);

        // Using the updated endpoint with path parameter
        Call<DiscussionResponse> call = apiService.getDiscussions(token, classId);
        call.enqueue(new Callback<DiscussionResponse>() {
            @Override
            public void onResponse(Call<DiscussionResponse> call, Response<DiscussionResponse> response) {
                showLoading(false);
                Log.d(TAG, "Discussion response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    DiscussionResponse discussionResponse = response.body();

                    if (discussionResponse.getStatus().equals("success")) {
                        List<DiscussionMessage> messages = discussionResponse.getData();
                        if (messages != null && !messages.isEmpty()) {
                            adapter.setMessages(messages);
                            recyclerView.scrollToPosition(messages.size() - 1);
                            tvNoMessages.setVisibility(View.GONE);
                            Log.d(TAG, "Successfully loaded " + messages.size() + " messages");
                        } else {
                            adapter.setMessages(new ArrayList<>());
                            tvNoMessages.setVisibility(View.VISIBLE);
                            Log.d(TAG, "No messages found for class ID: " + classId);
                        }
                    } else {
                        showError(discussionResponse.getMessage());
                        Log.e(TAG, "Error loading discussions: " + discussionResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error fetching discussions: " + errorBody);
                        showError("Failed to load discussions: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        showError("Failed to load discussions");
                    }
                }
            }

            @Override
            public void onFailure(Call<DiscussionResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching discussions", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (classId <= 0) {
            Toast.makeText(this, "Class information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        etMessage.setEnabled(false);

        // Show a sending indicator
        Toast.makeText(this, "Sending message...", Toast.LENGTH_SHORT).show();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        // Create the message request with the required fields
        MessageRequest request = new MessageRequest(classId, userId, messageText);
        Log.d(TAG, "Sending message to class ID: " + classId + ", user ID: " + userId);

        Call<MessageResponse> call = apiService.sendMessage(token, request);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                btnSend.setEnabled(true);
                etMessage.setEnabled(true);

                Log.d(TAG, "Send message response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();

                    if (messageResponse.getStatus().equals("success")) {
                        etMessage.setText("");
                        loadMessages();
                        Log.d(TAG, "Message sent successfully");
                    } else {
                        Toast.makeText(DiscussionActivity.this,
                                messageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error sending message: " + messageResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error sending message: " + errorBody);
                        Toast.makeText(DiscussionActivity.this,
                                "Failed to send message: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        Toast.makeText(DiscussionActivity.this,
                                "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                etMessage.setEnabled(true);
                Log.e(TAG, "Network error sending message", t);

                // Check if this is the specific JSON parsing error we're expecting
                if (t instanceof com.google.gson.JsonIOException &&
                        t.getMessage() != null &&
                        t.getMessage().contains("JSON document was not fully consumed")) {

                    // This is actually a success case - the server is returning two concatenated JSON objects
                    Log.d(TAG, "Message sent successfully despite JSON parsing error");
                    etMessage.setText("");
                    loadMessages();
                    Toast.makeText(DiscussionActivity.this,
                            "Message sent successfully", Toast.LENGTH_SHORT).show();
                }
                // Also handle MalformedJsonException as a success case
                else if (t.getMessage() != null && t.getMessage().contains("MalformedJsonException")) {
                    etMessage.setText("");
                    loadMessages();
                    Toast.makeText(DiscussionActivity.this,
                            "Message sent successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    // This is a genuine error
                    Toast.makeText(DiscussionActivity.this,
                            "Failed to send message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startMessageRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (classId > 0) {
                        loadMessages();
                    }
                });
            }
        }, 10000, 10000); // Refresh every 10 seconds
    }

    private void stopMessageRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvLoadingMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
            tvNoMessages.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            tvLoadingMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (tvNoMessages != null) {
            tvNoMessages.setVisibility(View.GONE);
        }
    }

    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_discussions);
    }
}

package com.educonnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.educonnect.adapter.RecyclerDiscussionAdapter;
import com.educonnect.api.ApiService;
import com.educonnect.api.RetrofitClient;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.DiscussionMessage;
import com.educonnect.model.DiscussionResponse;
import com.educonnect.model.MessageRequest;
import com.educonnect.model.MessageResponse;
import com.educonnect.utils.JwtUtils;
import com.educonnect.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDiscussionsActivity extends BaseActivity {
    private static final String TAG = "TeacherDiscussionsActivity";

    private Spinner spinnerClass;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private TextView tvNoMessages;
    private ConstraintLayout chatLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SessionManager sessionManager;
    private List<ClassResponse.ClassData> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private List<Integer> classIds = new ArrayList<>();
    private List<DiscussionMessage> messageList = new ArrayList<>();
    private RecyclerDiscussionAdapter discussionAdapter;
    private int selectedClassId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_discussions);

        sessionManager = new SessionManager(this);

        // Initialize views
        spinnerClass = findViewById(R.id.spinnerClass);
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);
        tvNoMessages = findViewById(R.id.tvNoMessages);
        chatLayout = findViewById(R.id.chat_container);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Hide chat layout initially
        if (chatLayout != null) {
            chatLayout.setVisibility(View.GONE);
        }

        // Set up RecyclerView
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            discussionAdapter = new RecyclerDiscussionAdapter(this, sessionManager.getUserId());
            recyclerView.setAdapter(discussionAdapter);
        }

        // Set up SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light
            );

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (selectedClassId != -1) {
                        refreshMessages(selectedClassId);
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Please select a class first",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Load classes for spinner
        loadClasses();

        // Set up class selection listener
        if (spinnerClass != null) {
            spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < classIds.size()) {
                        selectedClassId = classIds.get(position);
                        loadMessages(selectedClassId);
                        if (chatLayout != null) {
                            chatLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    if (chatLayout != null) {
                        chatLayout.setVisibility(View.GONE);
                    }
                }
            });
        }

        // Set up send button
        if (btnSend != null) {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        }
    }

    private void loadClasses() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<ClassResponse> call = apiService.getClasses(token);
        call.enqueue(new Callback<ClassResponse>() {
            @Override
            public void onResponse(Call<ClassResponse> call, Response<ClassResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ClassResponse classResponse = response.body();
                    if (classResponse.getStatus().equals("success") && classResponse.getData() != null) {
                        classList.clear();
                        classNames.clear();
                        classIds.clear();

                        classList.addAll(classResponse.getData());

                        for (ClassResponse.ClassData classData : classList) {
                            // Ensure we don't add null class names to the adapter
                            if (classData != null && classData.getClassName() != null) {
                                classNames.add(classData.getClassName());
                                classIds.add(classData.getId());
                            }
                        }

                        // Set up spinner adapter with non-null values
                        if (!classNames.isEmpty() && spinnerClass != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    TeacherDiscussionsActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    classNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerClass.setAdapter(adapter);
                        } else {
                            // Handle case when no classes are available
                            Toast.makeText(TeacherDiscussionsActivity.this,
                                    "No classes available",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Error: " + classResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading classes: " + errorBody);
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Failed to load classes: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ClassResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading classes", t);
                Toast.makeText(TeacherDiscussionsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages(int classId) {
        showLoading(true);
        if (tvNoMessages != null) {
            tvNoMessages.setVisibility(View.GONE);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<DiscussionResponse> call = apiService.getDiscussionMessages(token, classId);
        call.enqueue(new Callback<DiscussionResponse>() {
            @Override
            public void onResponse(Call<DiscussionResponse> call, Response<DiscussionResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    DiscussionResponse discussionResponse = response.body();
                    if (discussionResponse.getStatus().equals("success") && discussionResponse.getData() != null) {
                        messageList.clear();
                        messageList.addAll(discussionResponse.getData());
                        discussionAdapter.setMessages(messageList);

                        if (messageList.isEmpty() && tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.VISIBLE);
                        } else if (tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.GONE);
                            // Scroll to the bottom
                            if (recyclerView != null) {
                                recyclerView.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    } else {
                        if (tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Error: " + discussionResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (tvNoMessages != null) {
                        tvNoMessages.setVisibility(View.VISIBLE);
                    }
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading messages: " + errorBody);
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Failed to load messages: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<DiscussionResponse> call, Throwable t) {
                showLoading(false);
                if (tvNoMessages != null) {
                    tvNoMessages.setVisibility(View.VISIBLE);
                }
                Log.e(TAG, "Network error loading messages", t);
                Toast.makeText(TeacherDiscussionsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMessages(int classId) {
        // Don't show the main loading indicator for refresh
        if (tvNoMessages != null) {
            tvNoMessages.setVisibility(View.GONE);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<DiscussionResponse> call = apiService.getDiscussionMessages(token, classId);
        call.enqueue(new Callback<DiscussionResponse>() {
            @Override
            public void onResponse(Call<DiscussionResponse> call, Response<DiscussionResponse> response) {
                // Stop the refresh animation
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    DiscussionResponse discussionResponse = response.body();
                    if (discussionResponse.getStatus().equals("success") && discussionResponse.getData() != null) {
                        messageList.clear();
                        messageList.addAll(discussionResponse.getData());
                        discussionAdapter.setMessages(messageList);

                        if (messageList.isEmpty() && tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.VISIBLE);
                        } else if (tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.GONE);
                            // Scroll to the bottom
                            if (recyclerView != null) {
                                recyclerView.scrollToPosition(messageList.size() - 1);
                            }
                        }

                        // Show a toast to confirm refresh
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Messages refreshed",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (tvNoMessages != null) {
                            tvNoMessages.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Error: " + discussionResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (tvNoMessages != null) {
                        tvNoMessages.setVisibility(View.VISIBLE);
                    }
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error refreshing messages: " + errorBody);
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Failed to refresh messages: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<DiscussionResponse> call, Throwable t) {
                // Stop the refresh animation
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Log.e(TAG, "Network error refreshing messages", t);
                Toast.makeText(TeacherDiscussionsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedClassId == -1) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button to prevent multiple sends
        if (btnSend != null) {
            btnSend.setEnabled(false);
        }

        int userId = sessionManager.getUserId();

        // Create request object
        MessageRequest request = new MessageRequest(selectedClassId, userId, messageText);

        // Send API request
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String token = "Bearer " + sessionManager.getToken();

        Call<MessageResponse> call = apiService.sendMessage(token, request);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                // Re-enable send button
                if (btnSend != null) {
                    btnSend.setEnabled(true);
                }

                if (response.isSuccessful()) {
                    // Clear message input
                    if (etMessage != null) {
                        etMessage.setText("");
                    }

                    // Reload messages
                    loadMessages(selectedClassId);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error sending message: " + errorBody);
                        Toast.makeText(TeacherDiscussionsActivity.this,
                                "Failed to send message: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                // Re-enable send button
                if (btnSend != null) {
                    btnSend.setEnabled(true);
                }

                Log.e(TAG, "Network error sending message", t);
                Toast.makeText(TeacherDiscussionsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}

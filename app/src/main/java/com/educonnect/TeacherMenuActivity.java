package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.educonnect.utils.SessionManager;

public class TeacherMenuActivity extends BaseActivity {
    private static final String TAG = "TeacherMenuActivity";
    private SessionManager sessionManager;
    private CardView cardAssignments, cardResources, cardDiscussions, cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_menu);

        sessionManager = new SessionManager(this);

        // Check if user is logged in and is a teacher
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        if (!"teacher".equalsIgnoreCase(sessionManager.getUserRole())) {
            Toast.makeText(this, "Access denied. Teacher role required.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            finish();
            return;
        }

        // Initialize views
        cardAssignments = findViewById(R.id.cardAssignments);
        cardResources = findViewById(R.id.cardResources);
        cardDiscussions = findViewById(R.id.cardDiscussions);
        cardProfile = findViewById(R.id.cardProfile);

        // Set click listeners
        cardAssignments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeacherAssignments();
            }
        });

        cardResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeacherResources();
            }
        });

        cardDiscussions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTeacherDiscussions();
            }
        });

        cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfile();
            }
        });
    }

    private void navigateToTeacherAssignments() {
        Intent intent = new Intent(this, TeacherAssignmentsActivity.class);
        startActivity(intent);
    }

    private void navigateToTeacherResources() {
        Intent intent = new Intent(this, TeacherResourcesActivity.class);
        startActivity(intent);
    }

    private void navigateToTeacherDiscussions() {
        Intent intent = new Intent(this, TeacherDiscussionsActivity.class);
        startActivity(intent);
    }
}

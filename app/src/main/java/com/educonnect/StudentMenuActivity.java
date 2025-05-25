package com.educonnect;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import com.educonnect.model.User;

public class StudentMenuActivity extends BaseActivity {

    private TextView tvWelcome;
    private CardView cardProfile, cardAssignments, cardGrades, cardDiscussions, cardResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_menu);

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        cardProfile = findViewById(R.id.cardProfile);
        cardAssignments = findViewById(R.id.cardAssignments);
        cardGrades = findViewById(R.id.cardGrades);
        cardDiscussions = findViewById(R.id.cardDiscussions);
        cardResources = findViewById(R.id.cardResources);

        // Set welcome message
        User user = sessionManager.getUser();
        if (user != null) {
            String username = user.getUsername();
            if (username != null && !username.isEmpty()) {
                tvWelcome.setText("Welcome, " + username + "!");
            } else if (user.getEmail() != null) {
                tvWelcome.setText("Welcome, " + user.getEmail() + "!");
            }
        }

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Student Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // No back button on dashboard
        }

        // Set click listeners for cards
        cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfile();
            }
        });

        cardAssignments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAssignments();
            }
        });

        cardGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToGrades();
            }
        });

        cardDiscussions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDiscussions();
            }
        });

        cardResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToResources();
            }
        });
    }

    @Override
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return menu.findItem(R.id.action_dashboard);
    }

    @Override
    public void onBackPressed() {
        // Ask user if they want to exit the app
        // For now, just call super
        super.onBackPressed();
    }
}

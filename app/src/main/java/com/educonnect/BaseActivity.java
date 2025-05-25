package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.educonnect.utils.SessionManager;

/**
 * Base activity that implements common functionality for all student activities
 * including the student menu and navigation
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected SessionManager sessionManager;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            finish();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        // Setup toolbar if it exists in the layout
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            } catch (IllegalStateException e) {
                // Activity already has an action bar from the window decor
                Log.w("BaseActivity", "Error setting support action bar: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
        
        // Hide current activity's menu item
        MenuItem currentItem = getCurrentMenuItem(menu);
        if (currentItem != null) {
            currentItem.setVisible(false);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_dashboard) {
            navigateToDashboard();
            return true;
        } else if (itemId == R.id.action_assignments) {
            navigateToAssignments();
            return true;
        } else if (itemId == R.id.action_grades) {
            navigateToGrades();
            return true;
        } else if (itemId == R.id.action_discussions) {
            navigateToDiscussions();
            return true;
        } else if (itemId == R.id.action_resources) {
            navigateToResources();
            return true;
        } else if (itemId == R.id.action_profile) {
            navigateToProfile();
            return true;
        } else if (itemId == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the menu item corresponding to the current activity
     * Override this in each activity to return the appropriate menu item
     */
    protected MenuItem getCurrentMenuItem(Menu menu) {
        return null;
    }

    // Navigation methods
    protected void navigateToDashboard() {
        if (!(this instanceof StudentMenuActivity)) {
            Intent intent = new Intent(this, StudentMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    protected void navigateToAssignments() {
        if (!(this instanceof StudentAssignmentsActivity)) {
            Intent intent = new Intent(this, StudentAssignmentsActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToGrades() {
        if (!(this instanceof CheckGradesActivity)) {
            Intent intent = new Intent(this, CheckGradesActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToDiscussions() {
        if (!(this instanceof DiscussionActivity)) {
            Intent intent = new Intent(this, DiscussionActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToResources() {
        if (!(this instanceof ResourcesActivity)) {
            Intent intent = new Intent(this, ResourcesActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToProfile() {
        if (!(this instanceof ProfileActivity)) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    protected void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
        finish();
    }
}

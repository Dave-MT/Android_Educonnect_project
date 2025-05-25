package com.educonnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.educonnect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Short delay to allow the activity to render before redirecting
        new Handler(Looper.getMainLooper()).postDelayed(this::routeToAppropriateActivity, 100);
    }

    private void routeToAppropriateActivity() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
        } else {
            // Navigate based on role
            if (sessionManager.isTeacher()) {
                navigateToTeacherMenu();
            } else {
                navigateToStudentMenu();
            }
        }
        finish(); // Finish this activity so user can't navigate back to it
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    
    private void navigateToStudentMenu() {
        Intent intent = new Intent(MainActivity.this, StudentMenuActivity.class);
        startActivity(intent);
    }
    
    private void navigateToTeacherMenu() {
        Intent intent = new Intent(MainActivity.this, TeacherMenuActivity.class);
        startActivity(intent);
    }
}

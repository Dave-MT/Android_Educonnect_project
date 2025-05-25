package com.educonnect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.educonnect.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "EduConnectSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ROLE = "userRole";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    public void createSession(String token, User user, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, new Gson().toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }
    
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return new Gson().fromJson(userJson, User.class);
        }
        return null;
    }
    
    // Add getUserId method to retrieve the user ID
    public int getUserId() {
        // First try to get it from the stored User object
        User user = getUser();
        if (user != null && user.getId() > 0) {
            return user.getId();
        }
        
        // If that fails, try to extract it from the JWT token
        String token = getToken();
        if (token != null && !token.isEmpty()) {
            int userId = JwtUtils.getUserIdFromToken(token);
            if (userId > 0) {
                return userId;
            }
        }
        
        // If all else fails, return -1 to indicate failure
        Log.e(TAG, "Failed to retrieve user ID from session");
        return -1;
    }
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "student");
    }
    
    public boolean isTeacher() {
        return "teacher".equalsIgnoreCase(getUserRole());
    }
    
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

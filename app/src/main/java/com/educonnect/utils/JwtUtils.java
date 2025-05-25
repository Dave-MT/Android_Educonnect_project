package com.educonnect.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JwtUtils {
    private static final String TAG = "JwtUtils";
    private static final int DEFAULT_USER_ID = 2; // Fallback user ID if extraction fails

    /**
     * Extract user ID from JWT token
     * @param token JWT token
     * @return User ID or fallback ID if extraction fails
     */
    public static int getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            return DEFAULT_USER_ID;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Invalid token format");
                return DEFAULT_USER_ID;
            }

            // Add padding if needed
            String base64Payload = parts[1];
            while (base64Payload.length() % 4 != 0) {
                base64Payload += "=";
            }

            String payload = new String(Base64.decode(base64Payload, Base64.URL_SAFE), "UTF-8");
            JSONObject jsonPayload = new JSONObject(payload);

            // Try different possible field names for user ID
            if (jsonPayload.has("user_id")) {
                return jsonPayload.getInt("user_id");
            } else if (jsonPayload.has("id")) {
                return jsonPayload.getInt("id");
            } else if (jsonPayload.has("sub")) {
                return jsonPayload.getInt("sub");
            } else {
                Log.e(TAG, "No user ID found in token");
                return DEFAULT_USER_ID;
            }
        } catch (UnsupportedEncodingException | JSONException | IllegalArgumentException e) {
            Log.e(TAG, "Error extracting user ID from token: " + e.getMessage());
            return DEFAULT_USER_ID;
        }
    }

    /**
     * Extract role from JWT token
     * @param token JWT token
     * @return Role (teacher or student) or "student" as default
     */
    public static String extractRole(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            return "student";
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Invalid token format");
                return "student";
            }

            // Add padding if needed
            String base64Payload = parts[1];
            while (base64Payload.length() % 4 != 0) {
                base64Payload += "=";
            }

            String payload = new String(Base64.decode(base64Payload, Base64.URL_SAFE), "UTF-8");
            JSONObject jsonPayload = new JSONObject(payload);

            if (jsonPayload.has("role")) {
                return jsonPayload.getString("role");
            } else {
                Log.e(TAG, "No role found in token");
                return "student";
            }
        } catch (UnsupportedEncodingException | JSONException | IllegalArgumentException e) {
            Log.e(TAG, "Error extracting role from token: " + e.getMessage());
            return "student";
        }
    }

    /**
     * Extract user ID from token (alias for getUserIdFromToken)
     * @param token JWT token
     * @return User ID or fallback ID if extraction fails
     */
    public static int extractUserId(String token) {
        return getUserIdFromToken(token);
    }

    /**
     * Validate if token is properly formatted and not expired
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public static boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            // Add padding if needed
            String base64Payload = parts[1];
            while (base64Payload.length() % 4 != 0) {
                base64Payload += "=";
            }

            String payload = new String(Base64.decode(base64Payload, Base64.URL_SAFE), "UTF-8");
            JSONObject jsonPayload = new JSONObject(payload);

            // Check if token has expired
            if (jsonPayload.has("exp")) {
                long expTime = jsonPayload.getLong("exp");
                long currentTime = System.currentTimeMillis() / 1000;

                if (currentTime > expTime) {
                    Log.e(TAG, "Token has expired");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Debug method to print token payload
     * @param token JWT token
     */
    public static void debugToken(String token) {
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "Token is null or empty");
            return;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.d(TAG, "Invalid token format");
                return;
            }

            // Add padding if needed
            String base64Payload = parts[1];
            while (base64Payload.length() % 4 != 0) {
                base64Payload += "=";
            }

            String payload = new String(Base64.decode(base64Payload, Base64.URL_SAFE), "UTF-8");
            Log.d(TAG, "Token payload: " + payload);
        } catch (Exception e) {
            Log.e(TAG, "Error debugging token: " + e.getMessage());
        }
    }
}

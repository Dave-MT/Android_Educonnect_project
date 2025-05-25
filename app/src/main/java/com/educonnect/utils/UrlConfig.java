package com.educonnect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Utility class to manage API URL configuration
 * This allows changing the API base URL without rebuilding the app
 */
public class UrlConfig {
    private static final String TAG = "UrlConfig";
    private static final String PREF_NAME = "url_config";
    private static final String KEY_BASE_URL = "base_url";

    // Default URL if none is set
    private static final String DEFAULT_BASE_URL = "https://785c-102-213-68-239.ngrok-free.app/educonnect/api/";

    /**
     * Get the current base URL for API calls
     * @param context Application context
     * @return The base URL string
     */
    public static String getBaseUrl(Context context) {
        if (context == null) {
            Log.w(TAG, "Context is null, returning default URL");
            return DEFAULT_BASE_URL;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
    }

    /**
     * Set a new base URL for API calls
     * @param context Application context
     * @param baseUrl The new base URL
     * @return true if successful, false otherwise
     */
    public static boolean setBaseUrl(Context context, String baseUrl) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot save URL");
            return false;
        }

        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "Invalid URL provided");
            return false;
        }

        // Ensure URL ends with a slash
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_BASE_URL, baseUrl);
            boolean result = editor.commit();

            if (result) {
                Log.i(TAG, "Base URL updated to: " + baseUrl);
            } else {
                Log.e(TAG, "Failed to save base URL");
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error saving base URL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset the base URL to the default value
     * @param context Application context
     * @return true if successful, false otherwise
     */
    public static boolean resetToDefault(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot reset URL");
            return false;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_BASE_URL, DEFAULT_BASE_URL);
            boolean result = editor.commit();

            if (result) {
                Log.i(TAG, "Base URL reset to default: " + DEFAULT_BASE_URL);
            } else {
                Log.e(TAG, "Failed to reset base URL");
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error resetting base URL: " + e.getMessage());
            return false;
        }
    }
}

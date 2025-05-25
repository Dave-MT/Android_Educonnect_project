package com.educonnect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manage caching of data for offline access
 */
public class CacheManager {
    private static final String TAG = "CacheManager";
    private static final String PREF_NAME = "EduConnectCache";

    // Cache keys
    private static final String DISCUSSION_CACHE_PREFIX = "discussion_";
    private static final String CLASSES_CACHE = "classes_cache";
    private static final String ASSIGNMENTS_CACHE_PREFIX = "assignments_";
    private static final String LAST_UPDATE_PREFIX = "last_update_";

    // Cache expiration time (24 hours in milliseconds)
    private static final long CACHE_EXPIRATION = 24 * 60 * 60 * 1000;

    private final SharedPreferences preferences;
    private final Gson gson;

    public CacheManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Save discussion messages to cache for a specific class
     *
     * @param classId The class ID
     * @param messages The list of messages to cache
     */
    public <T> void cacheDiscussionMessages(int classId, List<T> messages) {
        try {
            String key = DISCUSSION_CACHE_PREFIX + classId;
            String json = gson.toJson(messages);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, json);
            editor.putLong(LAST_UPDATE_PREFIX + key, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cached " + messages.size() + " messages for class " + classId);
        } catch (Exception e) {
            Log.e(TAG, "Error caching discussion messages", e);
        }
    }

    /**
     * Get cached discussion messages for a specific class
     *
     * @param classId The class ID
     * @return The list of cached messages, or null if no cache exists
     */
    public <T> List<T> getCachedDiscussionMessages(int classId, Class<T> type) {
        try {
            String key = DISCUSSION_CACHE_PREFIX + classId;
            String json = preferences.getString(key, null);

            if (json == null) {
                return null;
            }

            // Check if cache is expired
            long lastUpdate = preferences.getLong(LAST_UPDATE_PREFIX + key, 0);
            if (System.currentTimeMillis() - lastUpdate > CACHE_EXPIRATION) {
                Log.d(TAG, "Cache expired for class " + classId);
                return null;
            }

            Type listType = TypeToken.getParameterized(List.class, type).getType();
            List<T> messages = gson.fromJson(json, listType);

            Log.d(TAG, "Retrieved " + (messages != null ? messages.size() : 0) + " cached messages for class " + classId);
            return messages;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached discussion messages", e);
            return null;
        }
    }

    /**
     * Save classes to cache
     *
     * @param classes The list of classes to cache
     */
    public <T> void cacheClasses(List<T> classes) {
        try {
            String json = gson.toJson(classes);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CLASSES_CACHE, json);
            editor.putLong(LAST_UPDATE_PREFIX + CLASSES_CACHE, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cached " + classes.size() + " classes");
        } catch (Exception e) {
            Log.e(TAG, "Error caching classes", e);
        }
    }

    /**
     * Get cached classes
     *
     * @return The list of cached classes, or null if no cache exists
     */
    public <T> List<T> getCachedClasses(Class<T> type) {
        try {
            String json = preferences.getString(CLASSES_CACHE, null);

            if (json == null) {
                return null;
            }

            // Check if cache is expired
            long lastUpdate = preferences.getLong(LAST_UPDATE_PREFIX + CLASSES_CACHE, 0);
            if (System.currentTimeMillis() - lastUpdate > CACHE_EXPIRATION) {
                Log.d(TAG, "Classes cache expired");
                return null;
            }

            Type listType = TypeToken.getParameterized(List.class, type).getType();
            List<T> classes = gson.fromJson(json, listType);

            Log.d(TAG, "Retrieved " + (classes != null ? classes.size() : 0) + " cached classes");
            return classes;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached classes", e);
            return null;
        }
    }

    /**
     * Save assignments to cache for a specific class
     *
     * @param classId The class ID
     * @param assignments The list of assignments to cache
     */
    public <T> void cacheAssignments(int classId, List<T> assignments) {
        try {
            String key = ASSIGNMENTS_CACHE_PREFIX + classId;
            String json = gson.toJson(assignments);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, json);
            editor.putLong(LAST_UPDATE_PREFIX + key, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "Cached " + assignments.size() + " assignments for class " + classId);
        } catch (Exception e) {
            Log.e(TAG, "Error caching assignments", e);
        }
    }

    /**
     * Get cached assignments for a specific class
     *
     * @param classId The class ID
     * @return The list of cached assignments, or null if no cache exists
     */
    public <T> List<T> getCachedAssignments(int classId, Class<T> type) {
        try {
            String key = ASSIGNMENTS_CACHE_PREFIX + classId;
            String json = preferences.getString(key, null);

            if (json == null) {
                return null;
            }

            // Check if cache is expired
            long lastUpdate = preferences.getLong(LAST_UPDATE_PREFIX + key, 0);
            if (System.currentTimeMillis() - lastUpdate > CACHE_EXPIRATION) {
                Log.d(TAG, "Assignments cache expired for class " + classId);
                return null;
            }

            Type listType = TypeToken.getParameterized(List.class, type).getType();
            List<T> assignments = gson.fromJson(json, listType);

            Log.d(TAG, "Retrieved " + (assignments != null ? assignments.size() : 0) + " cached assignments for class " + classId);
            return assignments;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached assignments", e);
            return null;
        }
    }

    /**
     * Clear all cached data
     */
    public void clearCache() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Clear cached data for a specific class
     *
     * @param classId The class ID
     */
    public void clearClassCache(int classId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(DISCUSSION_CACHE_PREFIX + classId);
        editor.remove(ASSIGNMENTS_CACHE_PREFIX + classId);
        editor.remove(LAST_UPDATE_PREFIX + DISCUSSION_CACHE_PREFIX + classId);
        editor.remove(LAST_UPDATE_PREFIX + ASSIGNMENTS_CACHE_PREFIX + classId);
        editor.apply();
        Log.d(TAG, "Cache cleared for class " + classId);
    }
}

package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateRequest {
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("bio")
    private String bio;

    public ProfileUpdateRequest(int userId, String username, String bio) {
        this.userId = userId;
        this.username = username;
        this.bio = bio;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}

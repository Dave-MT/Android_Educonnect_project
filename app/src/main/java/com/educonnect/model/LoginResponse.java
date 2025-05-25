package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("role")
    private String role;

    public boolean isSuccess() {
        return success || "success".equals(status);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}

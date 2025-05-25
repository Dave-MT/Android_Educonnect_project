package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class MessageRequest {
    @SerializedName("class_id")
    private int classId;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("message")
    private String message;
    
    // Default constructor
    public MessageRequest() {
        // Empty constructor for creating a new instance
    }
    
    public MessageRequest(int classId, int userId, String message) {
        this.classId = classId;
        this.userId = userId;
        this.message = message;
    }
    
    // Getters and setters
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

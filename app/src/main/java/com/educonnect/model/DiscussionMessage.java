package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class DiscussionMessage {
    @SerializedName("id")
    private int id;
    
    @SerializedName("class_id")
    private int classId;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("user_name")
    private String userName;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

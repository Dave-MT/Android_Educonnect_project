package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private MessageData data;
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public MessageData getData() {
        return data;
    }
    
    public static class MessageData {
        @SerializedName("id")
        private int id;
        
        @SerializedName("class_id")
        private int classId;
        
        @SerializedName("user_id")
        private int userId;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;
        
        public int getId() {
            return id;
        }
        
        public int getClassId() {
            return classId;
        }
        
        public int getUserId() {
            return userId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}

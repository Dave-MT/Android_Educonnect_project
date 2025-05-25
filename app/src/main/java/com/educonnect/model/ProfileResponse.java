package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private ProfileData data;
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public ProfileData getData() {
        return data;
    }
    
    public static class ProfileData {
        @SerializedName("first_name")
        private String firstName;
        
        @SerializedName("last_name")
        private String lastName;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("role")
        private String role;
        
        @SerializedName("class")
        private String classInfo;
        
        @SerializedName("created_at")
        private String createdAt;
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getClassInfo() {
            return classInfo;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
    }
}

package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class SingleResourceResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ResourceData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ResourceData getData() {
        return data;
    }

    public static class ResourceData {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("file_url")
        private String fileUrl;

        @SerializedName("class_id")
        private int classId;

        @SerializedName("uploaded_by")
        private int uploadedBy;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("first_name")
        private String firstName;

        @SerializedName("last_name")
        private String lastName;

        @SerializedName("role")
        private String role;

        @SerializedName("email")
        private String email;

        @SerializedName("user_id")
        private int userId;

        @SerializedName("class_name")
        private String className;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public int getClassId() {
            return classId;
        }

        public int getUploadedBy() {
            return uploadedBy;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getRole() {
            return role;
        }

        public String getEmail() {
            return email;
        }

        public int getUserId() {
            return userId;
        }

        public String getClassName() {
            return className;
        }

        // Helper method to get file path (for compatibility)
        public String getFilePath() {
            return fileUrl;
        }
    }
}

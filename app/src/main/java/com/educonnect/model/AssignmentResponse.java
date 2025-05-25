package com.educonnect.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AssignmentResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Assignment> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Assignment> getData() {
        return data;
    }

    public static class Assignment {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("class_id")
        private int classId;

        @SerializedName("teacher_id")
        private int teacherId;

        @SerializedName("file")
        private String file;

        @SerializedName("deadline")
        private String deadline;

        @SerializedName("due_date")
        private String dueDate;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("class_name")
        private String className;

        @SerializedName("is_submitted")
        private boolean isSubmitted;

        @SerializedName("status")
        private String status;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getClassId() {
            return classId;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public String getFile() {
            return file;
        }

        public String getDeadline() {
            return deadline != null ? deadline : dueDate;
        }

        public String getDueDate() {
            return dueDate != null ? dueDate : deadline;
        }

        public boolean isSubmitted() {
            return isSubmitted || "submitted".equals(status);
        }

        public String getClassName() {
            return className;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}


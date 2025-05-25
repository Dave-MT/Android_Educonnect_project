package com.educonnect.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ClassResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<ClassData> data;

    @SerializedName("class_id")
    private int classId;

    @SerializedName("class")
    private int classValue;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ClassData> getData() {
        return data;
    }

    public int getClassId() {
        // If we have a direct class_id field, use it
        if (classId != 0) {
            return classId;
        }

        // Otherwise, try to get it from the "class" field in the response
        if (classValue != 0) {
            return classValue;
        }

        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public static class ClassData {
        @SerializedName("id")
        private int id;

        @SerializedName("class_name")  // This matches the API response field name
        private String className;

        @SerializedName("name")        // Alternative field name
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("teacher_id")
        private int teacherId;

        @SerializedName("teacher_name")
        private String teacherName;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        public int getId() {
            return id;
        }

        public String getName() {
            // Return class_name if available, otherwise return name
            return className != null ? className : name;
        }

        // Add getClassName method that returns the actual class name
        public String getClassName() {
            // Return class_name if available, otherwise return name
            return className != null ? className : name;
        }

        public String getDescription() {
            return description;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        // Setter methods for completeness
        public void setId(int id) {
            this.id = id;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTeacherId(int teacherId) {
            this.teacherId = teacherId;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}

package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Assignment {
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

    @SerializedName("file_url")
    private String fileUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isSubmitted() {
        return isSubmitted || "submitted".equals(status);
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public String getFileUrl() {
        return fileUrl != null ? fileUrl : file;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    // Alias for deadline to maintain compatibility
    public String getDueDate() {
        return deadline;
    }

    public void setDueDate(String dueDate) {
        this.deadline = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

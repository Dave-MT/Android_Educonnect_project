package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class TeacherResourceRequest {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("resource_url")
    private String resourceUrl;

    @SerializedName("class_id")
    private int classId;

    @SerializedName("uploaded_by")
    private int uploadedBy;

    @SerializedName("teacher_id")
    private int teacherId;

    // Constructors
    public TeacherResourceRequest() {}

    public TeacherResourceRequest(String title, String description, String resourceUrl, int classId, int uploadedBy) {
        this.title = title;
        this.description = description;
        this.resourceUrl = resourceUrl;
        this.classId = classId;
        this.uploadedBy = uploadedBy;
    }

    // Getters and setters
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

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}

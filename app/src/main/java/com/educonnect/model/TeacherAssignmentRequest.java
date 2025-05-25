package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class TeacherAssignmentRequest {
    @SerializedName("id")
    private Integer id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("class_id")  // This matches your JS: class_id: classId
    private int classId;

    @SerializedName("deadline")  // This matches your JS: deadline: deadline
    private String deadline;

    @SerializedName("file")      // This matches your JS: file: fileBase64
    private String fileBase64;

    @SerializedName("teacher_id") // This matches your JS: teacher_id: userId
    private int teacherId;

    // Default constructor
    public TeacherAssignmentRequest() {
        // Empty constructor for creating a new instance
    }

    // Constructor for creating a new assignment (matches your JS structure)
    public TeacherAssignmentRequest(String title, String description, int classId, String deadline, String fileBase64, int teacherId) {
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.deadline = deadline;
        this.fileBase64 = fileBase64;
        this.teacherId = teacherId;
    }

    // Constructor for updating an existing assignment
    public TeacherAssignmentRequest(int id, String title, String description, int classId, String deadline, String fileBase64, int teacherId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.deadline = deadline;
        this.fileBase64 = fileBase64;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    // Added for compatibility with CreateAssignmentActivity (maps to deadline)
    public void setDueDate(String dueDate) {
        this.deadline = dueDate;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}

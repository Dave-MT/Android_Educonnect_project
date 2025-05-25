package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Submission {
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("assignment_id")
    private int assignmentId;

    @SerializedName("file_url")
    private String fileUrl;

    @SerializedName("created_at")
    private String submissionDate;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("class")
    private int classId;

    private String email;

    @SerializedName("assignment_title")
    private String assignmentTitle;

    @SerializedName("assignment_file")
    private String assignmentFile;

    @SerializedName("grade_id")
    private Integer gradeId;

    private String grade;
    private String feedback;
    private String status;

    // Constructors
    public Submission() {}

    public Submission(int id, int userId, int assignmentId, String fileUrl, String submissionDate) {
        this.id = id;
        this.userId = userId;
        this.assignmentId = assignmentId;
        this.fileUrl = fileUrl;
        this.submissionDate = submissionDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getAssignmentFile() {
        return assignmentFile;
    }

    public void setAssignmentFile(String assignmentFile) {
        this.assignmentFile = assignmentFile;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper method to get full student name
    public String getStudentName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "Unknown Student";
    }
}

package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class GradeRequest {
    @SerializedName("id")
    private Integer id; // grade_id for updates, null for new grades

    @SerializedName("assignment_submission_id")
    private int assignmentSubmissionId;

    @SerializedName("grade")
    private String grade;

    @SerializedName("feedback")
    private String feedback;

    public GradeRequest(Integer id, int assignmentSubmissionId, String grade, String feedback) {
        this.id = id;
        this.assignmentSubmissionId = assignmentSubmissionId;
        this.grade = grade;
        this.feedback = feedback;
    }

    // Constructor for new grades (no existing grade_id)
    public GradeRequest(int assignmentSubmissionId, String grade, String feedback) {
        this.id = null;
        this.assignmentSubmissionId = assignmentSubmissionId;
        this.grade = grade;
        this.feedback = feedback;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAssignmentSubmissionId() {
        return assignmentSubmissionId;
    }

    public void setAssignmentSubmissionId(int assignmentSubmissionId) {
        this.assignmentSubmissionId = assignmentSubmissionId;
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
}

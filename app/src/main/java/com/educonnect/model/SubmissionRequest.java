package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class SubmissionRequest {
    @SerializedName("assignment_id")
    private int assignmentId;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("submission_text")
    private String submissionText;

    public SubmissionRequest(int assignmentId, int userId, String submissionText) {
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.submissionText = submissionText;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSubmissionText() {
        return submissionText;
    }

    public void setSubmissionText(String submissionText) {
        this.submissionText = submissionText;
    }
}

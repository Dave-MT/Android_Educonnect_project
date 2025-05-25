package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GradeResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<Grade> data;
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<Grade> getData() {
        return data;
    }
    
    public static class Grade {
        @SerializedName("assignment_title")
        private String assignmentTitle;
        
        @SerializedName("grade")
        private String grade;
        
        @SerializedName("feedback")
        private String feedback;
        
        @SerializedName("submission_created_at")
        private String submissionCreatedAt;
        
        @SerializedName("grade_created_at")
        private String gradeCreatedAt;
        
        @SerializedName("submission_file")
        private String submissionFile;
        
        public String getAssignmentTitle() {
            return assignmentTitle;
        }
        
        public String getGrade() {
            return grade;
        }
        
        public String getFeedback() {
            return feedback;
        }
        
        public String getSubmissionCreatedAt() {
            return submissionCreatedAt;
        }
        
        public String getGradeCreatedAt() {
            return gradeCreatedAt;
        }
        
        public String getSubmissionFile() {
            return submissionFile;
        }
        
        public boolean isGraded() {
            return grade != null && !grade.isEmpty();
        }
    }
}

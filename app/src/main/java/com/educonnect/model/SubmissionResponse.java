package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmissionResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<Submission> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Submission> getData() {
        return data;
    }

    public void setData(List<Submission> data) {
        this.data = data;
    }
}

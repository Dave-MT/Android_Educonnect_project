package com.educonnect.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DiscussionResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<DiscussionMessage> data;
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<DiscussionMessage> getData() {
        return data;
    }
}

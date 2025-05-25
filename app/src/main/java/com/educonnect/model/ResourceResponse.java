package com.educonnect.model;

import com.educonnect.utils.UrlConfig;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResourceResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Resource> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Resource> getData() {
        return data;
    }

    public static class Resource {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("file_path")
        private String filePath;

        @SerializedName("file_url")
        private String fileUrl;

        @SerializedName("resource_url")
        private String resourceUrl;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("class_id")
        private int classId;

        @SerializedName("class_name")
        private String className;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public int getClassId() {
            return classId;
        }

        public String getClassName() {
            return className;
        }

        public String getResourceUrl() {
            // Use file_url if available, otherwise fall back to resource_url or file_path
            String url = fileUrl != null ? fileUrl : (resourceUrl != null ? resourceUrl : filePath);

            if (url == null || url.isEmpty()) {
                return null;
            }

            // Clean up the URL path
            String cleanUrl = cleanFilePath(url);

            // If it's already a full URL, return as is
            if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                return cleanUrl;
            }

            // Construct the full download URL
            String baseUrl = getBaseUrlForDownload();
            return baseUrl + cleanUrl;
        }

        private String cleanFilePath(String path) {
            if (path == null) return "";

            // Remove server-specific paths and normalize
            path = path.replace("/var/www/html/educonnect/api/../", "");
            path = path.replace("/var/www/html/educonnect/", "");
            path = path.replace("../", "");

            // Ensure it starts with uploads/ if it's a file
            if (!path.startsWith("uploads/") && !path.startsWith("http")) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (!path.startsWith("uploads/")) {
                    path = "uploads/" + path;
                }
            }

            return path;
        }

        private String getBaseUrlForDownload() {
            // Get base URL and modify it for file downloads
            String baseUrl = "https://dc75-102-208-97-134.ngrok-free.app/educonnect/";
            return baseUrl;
        }

        public void setResourceUrl(String resourceUrl) {
            this.resourceUrl = resourceUrl;
        }
    }
}

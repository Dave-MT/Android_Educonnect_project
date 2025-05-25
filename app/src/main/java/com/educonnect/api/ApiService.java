package com.educonnect.api;

import com.educonnect.model.Assignment;
import com.educonnect.model.AssignmentResponse;
import com.educonnect.model.Class;
import com.educonnect.model.ClassResponse;
import com.educonnect.model.DiscussionResponse;
import com.educonnect.model.GradeRequest;
import com.educonnect.model.GradeResponse;
import com.educonnect.model.LoginRequest;
import com.educonnect.model.LoginResponse;
import com.educonnect.model.MessageRequest;
import com.educonnect.model.MessageResponse;
import com.educonnect.model.ProfileResponse;
import com.educonnect.model.ResourceResponse;
import com.educonnect.model.SingleResourceResponse;
import com.educonnect.model.SubmissionRequest;
import com.educonnect.model.SubmissionResponse;
import com.educonnect.model.TeacherAssignmentRequest;
import com.educonnect.model.TeacherResourceRequest;
import com.educonnect.model.User;
import com.educonnect.EditAssignmentActivity;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Authentication
    @POST("login.php")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // Profile endpoint
    @GET("profile.php")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token);

    // User endpoints
    @GET("user.php")
    Call<List<User>> getUsers(@Header("Authorization") String token);

    @GET("user.php/{id}")
    Call<User> getUserById(@Path("id") int userId, @Header("Authorization") String token);

    // Get class ID for a user - Updated to use the correct parameter
    @GET("user.php")
    Call<ClassResponse> getUserClassByUserId(@Header("Authorization") String token, @Query("get_class_user_id") int userId);

    // Class endpoints - Updated return type to match expected usage
    @GET("classes.php")
    Call<ClassResponse> getClasses(@Header("Authorization") String token);

    @GET("classes.php/{id}")
    Call<Class> getClassById(@Path("id") int classId, @Header("Authorization") String token);

    // Assignment endpoints
    @GET("assignment.php")
    Call<List<Assignment>> getAssignments(@Header("Authorization") String token);

    @GET("assignment.php")
    Call<Assignment> getAssignmentById(@Query("id") int assignmentId, @Header("Authorization") String token);

    // Added getAssignment method as an alias for getAssignmentById
    @GET("assignment.php")
    Call<Assignment> getAssignment(@Header("Authorization") String token, @Query("id") int assignmentId);

    // Added getSingleAssignment method for the correct response format
    @GET("assignment.php")
    Call<EditAssignmentActivity.SingleAssignmentResponse> getSingleAssignment(@Header("Authorization") String token, @Query("id") int assignmentId);

    @GET("assignment.php/class/{classId}")
    Call<List<Assignment>> getAssignmentsByClass(@Path("classId") int classId, @Header("Authorization") String token);

    // Submission endpoints
    @POST("submission.php")
    Call<SubmissionResponse> submitAssignment(@Body SubmissionRequest submissionRequest, @Header("Authorization") String token);

    // Student assignments endpoint
    @GET("assignment.php")
    Call<AssignmentResponse> getAssignments(@Header("Authorization") String token, @Query("user_id") int userId);

    // Grades endpoint
    @GET("check_grades.php")
    Call<GradeResponse> getGrades(@Header("Authorization") String token, @Query("user_id") int userId);

    // Discussion endpoints - Updated to match the exact format required
    @GET("discussion.php/{classId}")
    Call<DiscussionResponse> getDiscussions(@Header("Authorization") String token, @Path("classId") int classId);

    // Added method for getting discussion messages
    @GET("discussion.php/{classId}")
    Call<DiscussionResponse> getDiscussionMessages(@Header("Authorization") String token, @Path("classId") int classId);

    // Updated to use the correct endpoint for sending messages
    @POST("discussion.php")
    Call<MessageResponse> sendMessage(@Header("Authorization") String token, @Body MessageRequest messageRequest);

    // Resources endpoint
    @GET("resource.php")
    Call<ResourceResponse> getResources(@Header("Authorization") String token);

    // Get resource by ID - Updated to use SingleResourceResponse
    @GET("resource.php")
    Call<SingleResourceResponse> getResourceById(@Header("Authorization") String token, @Query("id") int resourceId);

    // Submit assignment with file
    @Multipart
    @POST("assignment_submission.php")
    Call<SubmissionResponse> submitAssignmentWithFile(
            @Header("Authorization") String token,
            @Part("assignment_id") RequestBody assignmentId,
            @Part("user_id") RequestBody userId,
            @Part("comment") RequestBody comment,
            @Part MultipartBody.Part file
    );

    // TEACHER ENDPOINTS - UPDATED TO MATCH CORRECT BACKEND ENDPOINTS

    // Teacher assignments - Updated to use assignment.php
    @GET("assignment.php")
    Call<AssignmentResponse> getTeacherAssignments(@Header("Authorization") String token);

    // Added method for getting teacher assignments by class - Updated to use assignment.php
    @GET("assignment.php")
    Call<AssignmentResponse> getTeacherAssignmentsByClass(
            @Header("Authorization") String token,
            @Query("class_id") int classId,
            @Query("teacher_id") int teacherId
    );

    // Create assignment - Updated to use assignment.php
    @POST("assignment.php")
    Call<MessageResponse> createAssignment(@Header("Authorization") String token, @Body TeacherAssignmentRequest request);

    // Update assignment (JSON version) - Updated to use assignment.php
    @PUT("assignment.php")
    Call<MessageResponse> updateAssignment(@Header("Authorization") String token, @Body TeacherAssignmentRequest request);

    // Update assignment with JSON body - NEW METHOD
    @PUT("assignment.php")
    Call<MessageResponse> updateAssignmentJson(@Header("Authorization") String token, @Body RequestBody jsonBody);

    // Update assignment (Multipart version for file uploads) - Updated to use assignment.php
    @Multipart
    @PUT("assignment.php")
    Call<MessageResponse> updateAssignment(
            @Header("Authorization") String token,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );

    // Update assignment (Multipart version without file) - Use PUT method
    @Multipart
    @PUT("assignment.php")
    Call<MessageResponse> updateAssignmentWithoutFile(
            @Header("Authorization") String token,
            @PartMap Map<String, RequestBody> partMap
    );

    // Teacher resources - Updated to use resource.php
    @GET("resource.php")
    Call<ResourceResponse> getTeacherResources(@Header("Authorization") String token);

    // Create resource - Updated to use resource.php
    @POST("resource.php")
    Call<MessageResponse> createResource(@Header("Authorization") String token, @Body TeacherResourceRequest request);

    // Create resource with file upload
    @Multipart
    @POST("resource.php")
    Call<MessageResponse> createResourceWithFile(
            @Header("Authorization") String token,
            @Part("title") RequestBody title,
            @Part("class_id") RequestBody classId,
            @Part("uploaded_by") RequestBody uploadedBy,
            @Part MultipartBody.Part file
    );

    // Update resource - Updated to use resource.php
    @PUT("resource.php")
    Call<MessageResponse> updateResource(@Header("Authorization") String token, @Body TeacherResourceRequest request);

    // Class discussions - Updated to use classes.php
    @GET("classes.php")
    Call<List<Class>> getDiscussionClasses(@Header("Authorization") String token);

    // Grade submissions - Updated to use assignment_submission.php
    @GET("assignment_submission.php")
    Call<SubmissionResponse> getSubmissionsToGrade(
            @Header("Authorization") String token,
            @Query("assignment_id") int assignmentId
    );

    // Added getSubmissions method - FIXED to include Authorization header
    @GET("assignment_submission.php")
    Call<SubmissionResponse> getSubmissions(@Header("Authorization") String token, @Query("assignment_id") int assignmentId);

    // Submit grade - Updated to use grade.php
    @POST("grade.php")
    Call<MessageResponse> submitGrade(@Header("Authorization") String token, @Body SubmissionRequest request);

    // Grade submission - Updated to use grade.php (for backward compatibility)
    @POST("grade.php")
    Call<GradeResponse> gradeSubmission(@Header("Authorization") String token, @Body GradeRequest request);

    // Create new grade - Updated to use grade.php
    @POST("grade.php")
    Call<MessageResponse> createGrade(@Header("Authorization") String token, @Body GradeRequest request);

    // Update existing grade - Updated to use grade.php
    @PUT("grade.php")
    Call<MessageResponse> updateGrade(@Header("Authorization") String token, @Body GradeRequest request);

    // Delete assignment - Using @HTTP to allow DELETE with body
    @HTTP(method = "DELETE", path = "assignment.php", hasBody = true)
    Call<MessageResponse> deleteAssignment(@Header("Authorization") String token, @Body Map<String, Integer> requestBody);

    // Delete resource - Updated to use @HTTP with JSON body instead of query parameter
    @HTTP(method = "DELETE", path = "resource.php", hasBody = true)
    Call<MessageResponse> deleteResource(@Header("Authorization") String token, @Body Map<String, Integer> requestBody);
}

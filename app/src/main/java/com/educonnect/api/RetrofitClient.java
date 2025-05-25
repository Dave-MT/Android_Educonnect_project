package com.educonnect.api;

import android.content.Context;

import com.educonnect.utils.UrlConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Base URL for the API - now using the UrlConfig utility
    private static final String DEFAULT_BASE_URL = "https://dc75-102-208-97-134.ngrok-free.app/educonnect/api/";
    private static Retrofit retrofit = null;
    private static Context appContext = null;

    // Method to initialize the client with application context
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        // Force recreation of the Retrofit instance with the new context
        retrofit = null;
    }

    // Method to get the Retrofit client instance
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create a logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configure Gson to be lenient when parsing JSON and handle errors gracefully
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .serializeNulls()
                    .create();

            // Add a response interceptor to clean up malformed JSON
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        okhttp3.Response response = chain.proceed(chain.request());
                        if (response.body() != null && response.header("Content-Type", "").contains("application/json")) {
                            String responseBody = response.body().string();

                            // Clean up concatenated JSON responses
                            if (responseBody.contains("}{")) {
                                // Take only the first JSON object
                                int firstJsonEnd = responseBody.indexOf("}{") + 1;
                                responseBody = responseBody.substring(0, firstJsonEnd);
                            }

                            // Create new response with cleaned body
                            return response.newBuilder()
                                    .body(okhttp3.ResponseBody.create(
                                            response.body().contentType(),
                                            responseBody
                                    ))
                                    .build();
                        }
                        return response;
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Get the base URL from UrlConfig if context is available, otherwise use default
            String baseUrl = (appContext != null)
                    ? UrlConfig.getBaseUrl(appContext)
                    : DEFAULT_BASE_URL;

            // Build Retrofit with the lenient Gson converter
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // Add a method to get the base URL
    public static String getBaseUrl() {
        return (appContext != null)
                ? UrlConfig.getBaseUrl(appContext)
                : DEFAULT_BASE_URL;
    }

    // Method to update the base URL and recreate the Retrofit instance
    public static void updateBaseUrl(String newBaseUrl) {
        if (appContext != null) {
            UrlConfig.setBaseUrl(appContext, newBaseUrl);
            // Force recreation of the Retrofit instance
            retrofit = null;
        }
    }

    // Method to reset the Retrofit instance (useful when changing URLs)
    public static void resetInstance() {
        retrofit = null;
    }
}

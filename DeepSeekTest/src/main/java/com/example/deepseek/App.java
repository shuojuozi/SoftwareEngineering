package com.example.deepseek;

import okhttp3.*;
import java.io.IOException;
import java.util.logging.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String API_KEY = "sk-4b15920ec4f54108a7c5ad967f42cc27"; // Store your API key
    
    public static void main(String[] args) {
        
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");

        String jsonBody = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"You are a helpful assistant\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"Hi\",\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"temperature\": 1,\n" +
                "  \"tool_choice\": \"none\",\n" +
                "  \"stream\": false\n" +
                "}";

        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY) // Note the "Bearer " prefix
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Request failed with status: " + response.code());
                System.out.println("Error message: " + response.body().string());
                if (response.code() == 401) {
                    System.out.println("Authentication failed. Please check your API key.");
                }
            } else {
                System.out.println("Response: " + response.body().string());
            }
        } catch (IOException e) {
            System.err.println("Error making request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

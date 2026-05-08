package com.example.iaso;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConvexApiHelper {

    // ==================== CONFIGURATION ====================

    private static final String TAG = "ConvexApiHelper";
    private static final String CONVEX_URL = "https://neighborly-chihuahua-847.convex.cloud";
    private static final String CONVEX_ACTION_PATH = "workhorse:getClaudeResponse";
    private static final int DEFAULT_DAILY_MINUTES = 30;
    private static final int DEFAULT_TOTAL_DAYS = 30;
    private static final Pattern DAILY_TIME_PATTERN =
            Pattern.compile("Daily\\s*time:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_DAYS_PATTERN =
            Pattern.compile("Total\\s*days:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    // ==================== SETUP ====================

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // ==================== CALLBACK INTERFACE ====================

    public interface ClaudeResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    // ==================== CONSTRUCTOR ====================

    public ConvexApiHelper() {
        this.client = new OkHttpClient();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==================== OLD METHOD (keep this, don't delete) ====================

    /**
     * OLD single-message method — kept for backward compatibility
     * Do NOT remove until workhorse.java is fully migrated
     */
    public void sendMessageToClaude(String userMessage, ClaudeResponseCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject argsObject = new JSONObject();
                argsObject.put("userMessage", userMessage);

                JSONObject requestJson = new JSONObject();
                requestJson.put("path", CONVEX_ACTION_PATH);
                requestJson.put("args", argsObject);

                RequestBody body = RequestBody.create(requestJson.toString(), JSON);
                Request request = new Request.Builder()
                        .url(CONVEX_URL + "/api/action")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        postError(callback, "Server error: " + response.code());
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.optString("status", "");

                    if ("success".equals(status)) {
                        String claudeResponse = jsonResponse.optString("value", "");
                        postSuccess(callback, claudeResponse);
                    } else {
                        String errorMessage = jsonResponse.optString("errorMessage", "Unknown error from Convex");
                        postError(callback, "Convex error: " + errorMessage);
                    }
                }

            } catch (IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            } catch (JSONException e) {
                postError(callback, "Error parsing response: " + e.getMessage());
            }
        });
    }

    // ==================== MATRIX METHOD (system + user split) ====================

    /**
     * For MatrixEngine — sends system prompt and user message separately
     * System = rules/schema, User = just the goal data
     */
    public void sendMessageToClaude(
            String systemPrompt,
            String userMessage,
            ClaudeResponseCallback callback) {

        executor.execute(() -> {
            try {
                JSONArray messagesArray = new JSONArray();
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                messagesArray.put(userMsg);

                int dailyMinutes = extractFirstInt(userMessage, DAILY_TIME_PATTERN, DEFAULT_DAILY_MINUTES, "dailyMinutes");
                int totalDays = extractFirstInt(userMessage, TOTAL_DAYS_PATTERN, DEFAULT_TOTAL_DAYS, "totalDays");

                JSONObject contextObj = new JSONObject();
                contextObj.put("dailyMinutes", dailyMinutes);
                contextObj.put("targetDays", totalDays);
                contextObj.put("phase", "generating");

                JSONObject argsObject = new JSONObject();
                argsObject.put("messages", messagesArray);
                argsObject.put("context", contextObj);

                JSONObject requestJson = new JSONObject();
                requestJson.put("path", CONVEX_ACTION_PATH);
                requestJson.put("args", argsObject);

                RequestBody body = RequestBody.create(requestJson.toString(), JSON);
                Request request = new Request.Builder()
                        .url(CONVEX_URL + "/api/action")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        postError(callback, "Server error: " + response.code());
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.optString("status", "");

                    if ("success".equals(status)) {
                        postSuccess(callback, jsonResponse.optString("value", ""));
                    } else {
                        postError(callback, "Convex error: " + jsonResponse.optString("errorMessage", "Unknown error"));
                    }
                }

            } catch (IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            } catch (JSONException e) {
                postError(callback, "Error building request: " + e.getMessage());
            }
        });
    }

    // ==================== NEW METHOD (multi-turn conversation) ====================

    /**
     * NEW method — sends full conversation history to Claude via Convex
     * @param messages    Full conversation history (user + assistant turns)
     * @param dailyMinutes  User's daily time commitment
     * @param targetDays    Days until target completion
     * @param phase         "refining" for Convo1.0, "generating" for milestones
     * @param callback      Where to send the response
     */
    public void sendConversation(
            ArrayList<ChatMessage> messages,
            int dailyMinutes,
            int targetDays,
            String phase,
            ClaudeResponseCallback callback) {

        executor.execute(() -> {
            try {
                // Build messages JSON array
                JSONArray messagesArray = new JSONArray();
                for (ChatMessage msg : messages) {
                    messagesArray.put(msg.toJson());
                }

                // Build context object
                JSONObject contextObj = new JSONObject();
                contextObj.put("dailyMinutes", dailyMinutes);
                contextObj.put("targetDays", targetDays);
                contextObj.put("phase", phase);

                // Build args
                JSONObject argsObject = new JSONObject();
                argsObject.put("messages", messagesArray);
                argsObject.put("context", contextObj);

                // Build full request
                JSONObject requestJson = new JSONObject();
                requestJson.put("path", CONVEX_ACTION_PATH);
                requestJson.put("args", argsObject);

                RequestBody body = RequestBody.create(requestJson.toString(), JSON);
                Request request = new Request.Builder()
                        .url(CONVEX_URL + "/api/action")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        postError(callback, "Server error: " + response.code());
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.optString("status", "");

                    if ("success".equals(status)) {
                        String claudeResponse = jsonResponse.optString("value", "");
                        postSuccess(callback, claudeResponse);
                    } else {
                        String errorMessage = jsonResponse.optString("errorMessage", "Unknown error");
                        postError(callback, "Convex error: " + errorMessage);
                    }
                }

            } catch (IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            } catch (JSONException e) {
                postError(callback, "Error building request: " + e.getMessage());
            }
        });
    }

    // ==================== HELPER METHODS ====================

    private void postSuccess(ClaudeResponseCallback callback, String response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }

    private void postError(ClaudeResponseCallback callback, String errorMessage) {
        mainHandler.post(() -> callback.onError(errorMessage));
    }

    /**
     * Extracts the first integer captured by the provided regex pattern.
     * Returns fallback when no match is found or parsing fails.
     */
    private int extractFirstInt(String input, Pattern pattern, int fallback, String fieldName) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) { }
        }
        Log.w(TAG, "Falling back to default " + fieldName + "=" + fallback);
        return fallback;
    }

    public void shutdown() {
        executor.shutdown();
    }
}

package com.example.iaso;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ConvexApiHelper
 *
 * This helper class handles all communication with the Convex backend.
 * It sends user messages to the Claude API via your Convex cloud function
 * and returns the response.
 *
 * HOW IT WORKS:
 * 1. The user types a message in the app
 * 2. This class sends that message to your Convex backend
 * 3. Convex forwards the message to Claude API
 * 4. Claude responds, and we pass that response back to your app
 *
 * BEGINNER TIP: Network calls must run on a background thread in Android.
 * We use ExecutorService to run the network call off the main UI thread,
 * then use a Handler to return the result back to the main thread.
 */
public class ConvexApiHelper {

    // ==================== CONFIGURATION ====================

    /**
     * Your Convex deployment URL
     * This is where your Convex cloud functions live
     */
    private static final String CONVEX_URL = "https://neighborly-chihuahua-847.convex.cloud";

    /**
     * The name of the Convex action that calls Claude
     * Format: "file:functionName" where 'file' is the .ts file in convex/ folder
     * and 'functionName' is the exported function
     */
    private static final String CONVEX_ACTION_PATH = "workhorse:getClaudeResponse";

    // ==================== SETUP ====================

    /**
     * JSON media type for HTTP requests
     * This tells the server we're sending JSON data
     */
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * OkHttp client for making network requests
     * We reuse one instance for efficiency (best practice)
     */
    private final OkHttpClient client;

    /**
     * ExecutorService runs tasks on background threads
     * Network calls MUST run off the main thread in Android
     */
    private final ExecutorService executor;

    /**
     * Handler posts results back to the main (UI) thread
     * Only the main thread can update UI elements
     */
    private final Handler mainHandler;

    // ==================== CALLBACK INTERFACE ====================

    /**
     * Callback interface for receiving API responses
     *
     * BEGINNER TIP: This is how we return data from an async operation.
     * Instead of returning a value directly (which we can't do with async code),
     * we call these methods when we have a result or an error.
     */
    public interface ClaudeResponseCallback {
        /**
         * Called when we successfully get a response from Claude
         * @param response The text response from Claude
         */
        void onSuccess(String response);

        /**
         * Called when something goes wrong
         * @param errorMessage Description of what went wrong
         */
        void onError(String errorMessage);
    }

    // ==================== CONSTRUCTOR ====================

    /**
     * Creates a new ConvexApiHelper instance
     *
     * Sets up:
     * - OkHttpClient for network requests
     * - ExecutorService with a single background thread
     * - Handler to communicate with the main UI thread
     */
    public ConvexApiHelper() {
        this.client = new OkHttpClient();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==================== MAIN METHOD ====================

    /**
     * Sends a message to Claude via your Convex backend
     *
     * This is the main method you'll call from your Activity.
     * It handles all the complexity of:
     * 1. Creating the JSON request body
     * 2. Making the HTTP POST request
     * 3. Parsing the response
     * 4. Returning the result on the main thread
     *
     * @param userMessage The message the user typed
     * @param callback Where to send the response (success or error)
     *
     * EXAMPLE USAGE:
     * <pre>
     * helper.sendMessageToClaude("Hello, Claude!", new ClaudeResponseCallback() {
     *     @Override
     *     public void onSuccess(String response) {
     *         // Display response in your TextView
     *         responseTextView.setText(response);
     *     }
     *
     *     @Override
     *     public void onError(String errorMessage) {
     *         // Show error to user
     *         Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
     *     }
     * });
     * </pre>
     */
    public void sendMessageToClaude(String userMessage, ClaudeResponseCallback callback) {
        // Run the network call on a background thread
        // BEGINNER TIP: We can't make network calls on the main thread
        // because it would freeze the app while waiting for a response
        executor.execute(() -> {
            try {
                // Step 1: Build the JSON request body
                // The Convex action endpoint expects this format:
                // { "path": "file:functionName", "args": { "userMessage": "..." } }
                JSONObject argsObject = new JSONObject();
                argsObject.put("userMessage", userMessage);

                JSONObject requestJson = new JSONObject();
                requestJson.put("path", CONVEX_ACTION_PATH);
                requestJson.put("args", argsObject);

                String jsonBody = requestJson.toString();

                // Step 2: Create the HTTP request
                // We POST to the Convex action endpoint with our JSON body
                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                        .url(CONVEX_URL + "/api/action")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Step 3: Execute the request and get the response
                // This is the actual network call - it waits for the server to respond
                try (Response response = client.newCall(request).execute()) {
                    // Step 4: Check if the request was successful
                    if (!response.isSuccessful()) {
                        // Something went wrong on the server side
                        String errorMsg = "Server error: " + response.code();
                        postError(callback, errorMsg);
                        return;
                    }

                    // Step 5: Read and parse the response body
                    String responseBody = response.body() != null ? response.body().string() : "";

                    // The Convex response comes wrapped in a JSON object with a "value" field
                    // Example response: { "status": "success", "value": "Claude's response here" }
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // Check if the request was successful
                    String status = jsonResponse.optString("status", "");
                    if ("success".equals(status)) {
                        // Extract Claude's actual response text
                        String claudeResponse = jsonResponse.optString("value", "");
                        postSuccess(callback, claudeResponse);
                    } else {
                        // The Convex function returned an error
                        String errorMessage = jsonResponse.optString("errorMessage", "Unknown error from Convex");
                        postError(callback, "Convex error: " + errorMessage);
                    }
                }

            } catch (IOException e) {
                // Network error (no internet, timeout, etc.)
                postError(callback, "Network error: " + e.getMessage());
            } catch (JSONException e) {
                // Error parsing JSON (shouldn't happen with valid responses)
                postError(callback, "Error parsing response: " + e.getMessage());
            }
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Posts a successful result back to the main thread
     *
     * BEGINNER TIP: We use mainHandler.post() to run code on the main thread.
     * This is necessary because we got our result on a background thread,
     * but we need to update the UI (which can only be done on the main thread).
     *
     * @param callback The callback to notify
     * @param response The successful response to send
     */
    private void postSuccess(ClaudeResponseCallback callback, String response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }

    /**
     * Posts an error result back to the main thread
     *
     * @param callback The callback to notify
     * @param errorMessage The error message to send
     */
    private void postError(ClaudeResponseCallback callback, String errorMessage) {
        mainHandler.post(() -> callback.onError(errorMessage));
    }

    /**
     * Cleans up resources when the helper is no longer needed
     *
     * BEGINNER TIP: Always clean up ExecutorService when you're done with it.
     * Call this in your Activity's onDestroy() method.
     */
    public void shutdown() {
        executor.shutdown();
    }
}

package com.example.iaso.matrix;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.iaso.ChatMessage;
import com.example.iaso.ConvexApiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatrixEngine {

    public interface MatrixEngineCallback {
        void onTimelineGenerated(MatrixGoal goal);
        void onError(String errorMessage);
    }

    private static final String TAG = "MatrixEngine";

    private final ConvexApiHelper convexApiHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public MatrixEngine() {
        convexApiHelper = new ConvexApiHelper();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateTimeline(
            Context context,
            String goalDescription,
            int dailyMinutes,
            int totalDays,
            MatrixEngineCallback callback) {

        if (context == null) {
            deliverError(callback, "context must not be null");
            return;
        }
        if (goalDescription == null || goalDescription.trim().isEmpty()) {
            deliverError(callback, "goalDescription must not be null or empty");
            return;
        }
        if (dailyMinutes <= 0) {
            deliverError(callback, "dailyMinutes must be a positive integer");
            return;
        }
        if (totalDays <= 0) {
            deliverError(callback, "totalDays must be a positive integer");
            return;
        }

        String trimmedGoal = goalDescription.trim();
        String userMessage = String.format(
                MatrixPrompts.TIMELINE_USER_TEMPLATE, trimmedGoal, dailyMinutes, totalDays);

        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", MatrixPrompts.TIMELINE_SYSTEM_PROMPT));
        messages.add(new ChatMessage("user", userMessage));

        Log.d(TAG, "SYSTEM_PROMPT: " + MatrixPrompts.TIMELINE_SYSTEM_PROMPT);
        Log.d(TAG, "USER_MESSAGE: " + userMessage);

        convexApiHelper.sendConversation(
                messages,
                dailyMinutes,
                totalDays,
                "generating",
                new ConvexApiHelper.ClaudeResponseCallback() {
                    @Override
                    public void onSuccess(String rawResponse) {
                        Log.d(TAG, "RAW_RESPONSE: " + rawResponse);
                        executor.execute(() -> {
                            try {
                                List<MatrixMilestone> milestones =
                                        parseMilestones(rawResponse, totalDays);
                                MatrixGoal goal = buildAndPersist(
                                        context, trimmedGoal, totalDays, milestones);
                                deliverSuccess(callback, goal);
                            } catch (Exception e) {
                                Log.e(TAG, "Parse/persist failed: " + e.getMessage(), e);
                                deliverError(callback, "Failed to process response: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Convex error: " + errorMessage);
                        deliverError(callback, errorMessage);
                    }
                });
    }

    public void shutdown() {
        convexApiHelper.shutdown();
        executor.shutdown();
    }

    private List<MatrixMilestone> parseMilestones(String rawResponse, int totalDays) {
        String text = rawResponse == null ? "" : rawResponse.trim();

        if (text.startsWith("```")) {
            text = text.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("```\\s*$", "").trim();
        }

        if (text.startsWith("[")) {
            try {
                JSONArray arr = new JSONArray(text);
                List<MatrixMilestone> milestones = fromJsonArray(arr);
                reconcileDaySum(milestones, totalDays);
                return milestones;
            } catch (JSONException e) {
                Log.w(TAG, "Primary JSON parse failed, trying regex fallback: " + e.getMessage());
            }
        }

        List<MatrixMilestone> fallback = regexFallback(text);
        if (!fallback.isEmpty()) {
            Log.w(TAG, "Regex fallback produced " + fallback.size() + " milestones");
            reconcileDaySum(fallback, totalDays);
            return fallback;
        }

        String preview = text.length() > 200 ? text.substring(0, 200) + "…" : text;
        throw new IllegalStateException("Cannot parse milestone list from response: " + preview);
    }

    private List<MatrixMilestone> fromJsonArray(JSONArray arr) throws JSONException {
        List<MatrixMilestone> result = new ArrayList<>();
        int startDay = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String name = obj.optString("name", "Milestone " + (i + 1));
            String description = obj.optString("description", "");
            int allocatedDays = obj.optInt("allocatedDays", 1);
            if (allocatedDays <= 0) allocatedDays = 1;

            MatrixMilestone m = new MatrixMilestone(
                    UUID.randomUUID().toString(),
                    "",
                    name,
                    description,
                    i,
                    allocatedDays,
                    0,
                    startDay
            );
            result.add(m);
            startDay += allocatedDays;
        }
        return result;
    }

    private List<MatrixMilestone> regexFallback(String text) {
        List<MatrixMilestone> result = new ArrayList<>();
        Pattern p = Pattern.compile("\\{[^{}]*\"name\"[^{}]*\\}", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        int startDay = 0;
        int index = 0;
        while (m.find()) {
            try {
                JSONObject obj = new JSONObject(m.group());
                String name = obj.optString("name", "Milestone " + (index + 1));
                String description = obj.optString("description", "");
                int allocatedDays = obj.optInt("allocatedDays", 1);
                if (allocatedDays <= 0) allocatedDays = 1;

                MatrixMilestone ms = new MatrixMilestone(
                        UUID.randomUUID().toString(),
                        "",
                        name,
                        description,
                        index,
                        allocatedDays,
                        0,
                        startDay
                );
                result.add(ms);
                startDay += allocatedDays;
                index++;
            } catch (JSONException ignored) {
                Log.w(TAG, "Regex fallback: skipping malformed block");
            }
        }
        return result;
    }

    private void reconcileDaySum(List<MatrixMilestone> milestones, int totalDays) {
        if (milestones.isEmpty()) return;
        int sum = 0;
        for (MatrixMilestone m : milestones) sum += m.getAllocatedDays();
        if (sum == totalDays) return;

        MatrixMilestone last = milestones.get(milestones.size() - 1);
        int adjusted = last.getAllocatedDays() + (totalDays - sum);
        if (adjusted <= 0) adjusted = 1;
        last.setAllocatedDays(adjusted);

        int recomputedStart = 0;
        for (MatrixMilestone m : milestones) {
            m.setStartDay(recomputedStart);
            recomputedStart += m.getAllocatedDays();
        }
        Log.w(TAG, "Day sum reconciled: was " + sum + ", adjusted to " + totalDays);
    }

    private MatrixGoal buildAndPersist(
            Context context,
            String goalDescription,
            int totalDays,
            List<MatrixMilestone> milestones) {

        String goalId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        MatrixGoal goal = new MatrixGoal(
                goalDescription,
                goalDescription,
                totalDays,
                now
        );
        goal.setGoalId(goalId);
        goal.setMilestones(milestones);

        for (MatrixMilestone m : milestones) {
            m.setParentGoalId(goalId);
        }

        MatrixStorage.saveGoal(context, goal);
        MatrixStorage.saveMilestones(context, goalId, milestones);

        int totalDaysElapsed = 0;
        int totalDaysRemaining = totalDays;
        MatrixSnapshot snapshot = new MatrixSnapshot(
                goalId,
                0,
                1,
                totalDaysElapsed,
                totalDaysRemaining,
                0
        );
        MatrixStorage.saveSnapshot(context, snapshot);

        Log.d(TAG, "Persisted MatrixGoal id=" + goalId +
                " milestones=" + milestones.size() +
                " totalDays=" + totalDays);

        return goal;
    }

    private void deliverSuccess(MatrixEngineCallback callback, MatrixGoal goal) {
        mainHandler.post(() -> callback.onTimelineGenerated(goal));
    }

    private void deliverError(MatrixEngineCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
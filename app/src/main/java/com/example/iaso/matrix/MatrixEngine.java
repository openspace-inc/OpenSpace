package com.example.iaso.matrix;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.iaso.ConvexApiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatrixEngine {

    private static final String TAG = "MatrixEngine";

    // ─── Callback ────────────────────────────────────────────────────────────

    public interface MatrixEngineCallback {
        void onTimelineGenerated(@NonNull MatrixGoal goal);
        void onError(@NonNull String errorMessage);
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public void generateTimeline(
            @NonNull Context context,
            @NonNull String goalDescription,
            int dailyMinutes,
            int totalDays,
            @NonNull MatrixEngineCallback callback
    ) {
        // System prompt — fixed rules, never changes
        String system = MatrixPrompts.TIMELINE_SYSTEM;

        // User message — just the goal data, changes every call
        String userTemplate = MatrixPrompts.TIMELINE_USER
                .replace("{goalDescription}", "%s")
                .replace("{dailyMinutes}", "%s")
                .replace("{totalDays}", "%s");
        String user = String.format(
                userTemplate,
                goalDescription,
                String.valueOf(dailyMinutes),
                String.valueOf(totalDays)
        );

        Log.d(TAG, "Generating timeline request: dailyMinutes=" + dailyMinutes
                + ", totalDays=" + totalDays
                + ", systemPromptLength=" + system.length()
                + ", userMessageLength=" + user.length());

        ConvexApiHelper api = new ConvexApiHelper();
        api.sendMessageToClaude(system, user, dailyMinutes, totalDays, new ConvexApiHelper.ClaudeResponseCallback() {

            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Received timeline response: responseLength="
                        + (response != null ? response.length() : 0));
                try {
                    List<MatrixMilestone> milestones = parseMilestones(response);

                    if (milestones.isEmpty()) {
                        callback.onError("No milestones could be parsed from the response.");
                        return;
                    }

                    // Validate day sum and normalize to totalDays when possible
                    int sum = 0;
                    for (MatrixMilestone m : milestones) sum += m.getAllocatedDays();
                    if (sum != totalDays) {
                        Log.w(TAG, "Day sum mismatch: expected " + totalDays + ", got " + sum);
                        boolean normalized = normalizeMilestoneDays(milestones, totalDays);
                        if (!normalized) {
                            callback.onError("Failed to normalize milestones to requested total days.");
                            return;
                        }
                    }

                    MatrixGoal goal = buildGoal(goalDescription, totalDays, milestones);
                    MatrixStorage.saveGoal(context, goal);

                    MatrixSnapshot snapshot = new MatrixSnapshot();
                    snapshot.setActiveGoalId(goal.getGoalId());
                    snapshot.setActiveMilestoneIndex(0);
                    snapshot.setCurrentDayInMilestone(1);
                    snapshot.setTotalDaysElapsed(0);
                    snapshot.setTotalDaysRemaining(totalDays);
                    snapshot.setBufferRemaining(0);
                    MatrixStorage.saveSnapshot(context, snapshot);

                    callback.onTimelineGenerated(goal);

                } catch (Exception e) {
                    Log.e(TAG, "Parse/build error: " + e.getMessage(), e);
                    callback.onError("Failed to build timeline: " + e.getMessage());
                } finally {
                    api.shutdown();
                }
            }

            @Override
            public void onError(String errorMessage) {
                try {
                    Log.e(TAG, "ConvexApiHelper error: " + errorMessage);
                    callback.onError(errorMessage);
                } finally {
                    api.shutdown();
                }
            }
        });
    }

    // ─── Parsing ─────────────────────────────────────────────────────────────

    /**
     * 3-layer parse fallback:
     * 1) Strict JSON array
     * 2) Bracket extraction (handles markdown fences or preamble)
     * 3) Regex object-by-object extraction (last resort)
     */
    private List<MatrixMilestone> parseMilestones(String raw) throws JSONException {
        String trimmed = raw.trim();

        // 1) Strict JSON array
        try {
            JSONArray array = new JSONArray(trimmed);
            List<MatrixMilestone> milestones = buildMilestonesFromArray(array);
            if (!milestones.isEmpty()) {
                Log.d(TAG, "Parsed via strict JSON path.");
                return milestones;
            }
        } catch (JSONException ignored) { }

        // 2) Bracket extraction
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start != -1 && end > start) {
            String extracted = trimmed.substring(start, end + 1);
            try {
                JSONArray array = new JSONArray(extracted);
                List<MatrixMilestone> milestones = buildMilestonesFromArray(array);
                if (!milestones.isEmpty()) {
                    Log.d(TAG, "Parsed via bracket extraction.");
                    return milestones;
                }
            } catch (JSONException ignored) { }
        }

        // 3) Regex fallback
        Log.w(TAG, "Falling back to regex milestone extraction.");
        return parseMilestonesViaRegex(trimmed);
    }

    private List<MatrixMilestone> parseMilestonesViaRegex(String raw) throws JSONException {
        Pattern blockPattern = Pattern.compile("\\{[^{}]*\"name\"[^{}]*\\}");
        Matcher matcher = blockPattern.matcher(raw);

        JSONArray array = new JSONArray();
        while (matcher.find()) {
            try {
                array.put(new JSONObject(matcher.group()));
            } catch (JSONException ignored) { }
        }

        if (array.length() == 0) {
            Log.e(TAG, "Regex extraction found no milestone blocks.");
        }

        return buildMilestonesFromArray(array);
    }

    /**
     * Converts a JSONArray of milestone objects into MatrixMilestone list.
     * Assigns UUIDs, orderIndex, and computes startDay from cumulative allocatedDays.
     */
    private List<MatrixMilestone> buildMilestonesFromArray(JSONArray array) throws JSONException {
        List<MatrixMilestone> milestones = new ArrayList<>();
        int startDay = 1;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            String name        = obj.optString("name", "Milestone " + (i + 1));
            String description = obj.optString("description", "");
            int allocatedDays  = Math.max(1, obj.optInt("allocatedDays", 1));

            // Parse dependencies — logged now, stored when MatrixMilestone supports it
            JSONArray deps = obj.optJSONArray("dependencies");
            if (deps != null && deps.length() > 0) {
                Log.d(TAG, "Milestone " + i + " dependencies: " + deps.toString());
            }

            MatrixMilestone milestone = new MatrixMilestone();
            milestone.setMilestoneId(UUID.randomUUID().toString());
            milestone.setName(name);
            milestone.setDescription(description);
            milestone.setOrderIndex(i);
            milestone.setAllocatedDays(allocatedDays);
            milestone.setBufferDays(0);
            milestone.setStartDay(startDay);

            milestones.add(milestone);
            startDay += allocatedDays;
        }

        return milestones;
    }

    private boolean normalizeMilestoneDays(List<MatrixMilestone> milestones, int totalDays) {
        int sum = 0;
        for (MatrixMilestone milestone : milestones) {
            sum += milestone.getAllocatedDays();
        }

        if (sum == totalDays) {
            return true;
        }

        if (sum < totalDays) {
            MatrixMilestone last = milestones.get(milestones.size() - 1);
            last.setAllocatedDays(last.getAllocatedDays() + (totalDays - sum));
            recomputeStartDays(milestones);
            return true;
        }

        int overflow = sum - totalDays;
        for (int i = milestones.size() - 1; i >= 0 && overflow > 0; i--) {
            MatrixMilestone milestone = milestones.get(i);
            int reducible = milestone.getAllocatedDays() - 1;
            if (reducible <= 0) {
                continue;
            }

            int reduction = Math.min(reducible, overflow);
            milestone.setAllocatedDays(milestone.getAllocatedDays() - reduction);
            overflow -= reduction;
        }

        if (overflow > 0) {
            return false;
        }

        recomputeStartDays(milestones);
        return true;
    }

    private void recomputeStartDays(List<MatrixMilestone> milestones) {
        int startDay = 1;
        for (MatrixMilestone milestone : milestones) {
            milestone.setStartDay(startDay);
            startDay += milestone.getAllocatedDays();
        }
    }

    // ─── Goal Assembly ───────────────────────────────────────────────────────

    private MatrixGoal buildGoal(String goalDescription, int totalDays, List<MatrixMilestone> milestones) {
        MatrixGoal goal = new MatrixGoal();
        String goalId = UUID.randomUUID().toString();
        goal.setGoalId(goalId);
        goal.setHabitName(goalDescription);
        goal.setGoalDescription(goalDescription);

        long now = System.currentTimeMillis();
        goal.setStartDate(now);
        goal.setTotalDays(totalDays);
        for (MatrixMilestone milestone : milestones) {
            milestone.setParentGoalId(goalId);
        }
        goal.setMilestones(milestones);

        return goal;
    }
}

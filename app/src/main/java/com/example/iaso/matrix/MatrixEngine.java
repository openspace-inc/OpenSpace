package com.example.iaso.matrix;

import android.content.Context;
import android.util.Log;

import com.example.iaso.network.ConvexApiHelper;
import com.example.iaso.model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MatrixEngine — The core AI pipeline for Matrix 1.0.
 *
 * <p>Responsible for taking a finalized goal description (output of Convo1.0) and
 * decomposing it into an ordered, time-allocated list of {@link MatrixMilestone} objects
 * by invoking Claude via {@link ConvexApiHelper}. The resulting {@link MatrixGoal} is
 * persisted to disk via {@link MatrixStorage} and returned to the caller through a
 * {@link MatrixEngineCallback}.
 *
 * <p>This class is fully decoupled from the UI layer — it holds no references to
 * Activities, Views, or Fragments and is safe to call from any context.
 *
 * <p>This class is not instantiable. All entry points are static.
 */
public final class MatrixEngine {

    private static final String TAG = "MatrixEngine";

    /** Utility class — do not instantiate. */
    private MatrixEngine() {
    }

    /**
     * Callback interface delivered after {@link #generateTimeline} completes.
     * Exactly one method will be called per invocation, never both.
     */
    public interface MatrixEngineCallback {

        /**
         * Called when the timeline has been successfully generated, persisted, and is
         * ready for downstream consumption by Fount1.0, Flux1.0, or the UI layer.
         *
         * @param goal A fully populated {@link MatrixGoal} containing the ordered
         *             milestone list and an initial {@link MatrixSnapshot} at position 0.
         */
        void onTimelineGenerated(MatrixGoal goal);

        /**
         * Called when any step in the pipeline fails — input validation, network error,
         * or JSON parse failure. The goal is not persisted when this fires.
         *
         * @param errorMessage A human-readable description of the failure reason,
         *                     suitable for logging or surface-level error display.
         */
        void onError(String errorMessage);
    }

    /**
     * Entry point for Matrix 1.0 timeline generation.
     *
     * <p>Constructs a structured prompt from the provided parameters, sends it to Claude
     * via Convex, parses the response into {@link MatrixMilestone} objects, assembles a
     * {@link MatrixGoal}, persists it via {@link MatrixStorage}, and delivers the result
     * to {@code callback}. The network call is asynchronous — this method returns
     * immediately and the callback fires on the thread used by {@link ConvexApiHelper}.
     *
     * <p>Input constraints:
     * <ul>
     *   <li>{@code goalDescription} must be non-null and non-empty.</li>
     *   <li>{@code dailyMinutes} and {@code totalDays} must both be positive.</li>
     * </ul>
     *
     * @param context          Android context used for {@link MatrixStorage} disk access.
     * @param goalDescription  The finalized goal text produced by Convo1.0.
     * @param dailyMinutes     The user's available daily time commitment in minutes.
     *                         Included in the prompt so Claude calibrates milestone scope.
     * @param totalDays        Total calendar days allocated to the goal. Milestone day
     *                         allocations in Claude's response must sum to this value.
     * @param callback         Receives the completed {@link MatrixGoal} or an error message.
     *                         May be null, in which case results are silently discarded.
     */
    public static void generateTimeline(
            Context context,
            String goalDescription,
            int dailyMinutes,
            int totalDays,
            MatrixEngineCallback callback
    ) {
        if (context == null || goalDescription == null || goalDescription.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid input: goalDescription must not be null or empty.");
            return;
        }
        if (dailyMinutes <= 0 || totalDays <= 0) {
            if (callback != null) callback.onError("Invalid input: dailyMinutes and totalDays must be positive.");
            return;
        }

        String userMessage = buildUserMessage(goalDescription, dailyMinutes, totalDays);

        Log.d(TAG, "generateTimeline — system prompt:\n" + MatrixPrompts.MATRIX_TIMELINE_PROMPT);
        Log.d(TAG, "generateTimeline — user message:\n" + userMessage);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", userMessage));

        ConvexApiHelper.sendConversation(
                MatrixPrompts.MATRIX_TIMELINE_PROMPT,
                messages,
                new ConvexApiHelper.ConvexCallback() {
                    @Override
                    public void onSuccess(String rawResponse) {
                        Log.d(TAG, "generateTimeline — raw response:\n" + rawResponse);
                        handleResponse(context, rawResponse, goalDescription, totalDays, callback);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "generateTimeline — Convex error: " + error);
                        if (callback != null) callback.onError(error);
                    }
                }
        );
    }

    /**
     * Constructs the user-turn message sent to Claude.
     *
     * <p>Injects goal description, daily minutes, and total days into a plain-text
     * format that pairs with the structured system prompt in {@link MatrixPrompts}.
     * The explicit day-sum constraint is repeated here as a final enforcement signal.
     *
     * @param goalDescription The finalized goal text.
     * @param dailyMinutes    Daily time commitment in minutes.
     * @param totalDays       Total calendar days for the goal.
     * @return A formatted string ready to send as the user message.
     */
    private static String buildUserMessage(String goalDescription, int dailyMinutes, int totalDays) {
        return "Goal: " + goalDescription + "\n" +
                "Daily time available: " + dailyMinutes + " minutes\n" +
                "Total timeline: " + totalDays + " days\n\n" +
                "Decompose this goal into an ordered milestone sequence. " +
                "The allocatedDays across all milestones must sum to exactly " + totalDays + ".";
    }

    /**
     * Processes Claude's raw response string into a persisted {@link MatrixGoal}.
     *
     * <p>Parsing is attempted in two stages:
     * <ol>
     *   <li>Direct extraction — locate the outermost JSON array brackets and parse.</li>
     *   <li>Regex extraction — if direct parse fails, scan for individual milestone
     *       JSON objects matching the expected field signature (graceful degradation).</li>
     * </ol>
     * If both stages fail, {@code callback.onError} is invoked and no data is persisted.
     *
     * @param context         Android context for {@link MatrixStorage}.
     * @param rawResponse     The raw string returned by Claude via Convex.
     * @param goalDescription Original goal text used to populate the {@link MatrixGoal}.
     * @param totalDays       Total days used to initialise the goal and snapshot.
     * @param callback        Receives the completed goal or an error.
     */
    private static void handleResponse(
            Context context,
            String rawResponse,
            String goalDescription,
            int totalDays,
            MatrixEngineCallback callback
    ) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            if (callback != null) callback.onError("Empty response from Claude.");
            return;
        }

        JSONArray parsed = attemptDirectParse(rawResponse);
        if (parsed == null) {
            Log.w(TAG, "Direct JSON parse failed — attempting regex extraction.");
            parsed = attemptRegexExtraction(rawResponse);
        }

        if (parsed == null || parsed.length() == 0) {
            if (callback != null) callback.onError("Failed to parse milestone data from Claude response.");
            return;
        }

        List<MatrixMilestone> milestones = buildMilestones(parsed);
        if (milestones == null || milestones.isEmpty()) {
            if (callback != null) callback.onError("No valid milestones could be constructed from Claude response.");
            return;
        }

        MatrixGoal goal = new MatrixGoal(
                goalDescription,
                goalDescription,
                totalDays,
                System.currentTimeMillis()
        );
        goal.setMilestones(milestones);

        MatrixStorage.saveGoal(context, goal);
        MatrixStorage.saveMilestones(context, goal.getGoalId(), milestones);

        // Initialise the snapshot at the very start of the timeline — milestone 0, day 1,
        // zero elapsed, full days remaining, and no buffer consumed.
        MatrixSnapshot snapshot = new MatrixSnapshot(
                goal.getGoalId(),
                0,
                1,
                0,
                totalDays,
                0
        );
        MatrixStorage.saveSnapshot(context, snapshot);

        Log.d(TAG, "generateTimeline — goal saved: " + goal.getGoalId() + " with " + milestones.size() + " milestones.");

        if (callback != null) callback.onTimelineGenerated(goal);
    }

    /**
     * Attempts to extract and parse a JSON array directly from the raw response string.
     *
     * <p>Locates the first {@code [} and last {@code ]} in the response and parses the
     * substring between them. This handles cases where Claude prepends or appends a small
     * amount of prose around the array.
     *
     * @param raw The raw Claude response string.
     * @return A parsed {@link JSONArray}, or {@code null} if no valid array was found.
     */
    private static JSONArray attemptDirectParse(String raw) {
        String trimmed = raw.trim();
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) return null;

        String candidate = trimmed.substring(start, end + 1);
        try {
            return new JSONArray(candidate);
        } catch (JSONException e) {
            Log.w(TAG, "attemptDirectParse failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fallback parser that extracts individual milestone JSON blocks via regex.
     *
     * <p>Used when {@link #attemptDirectParse} fails — for example, if Claude wraps
     * milestones in prose, uses markdown fences, or produces a structurally broken outer
     * array. The pattern matches any flat JSON object containing both {@code "name"} and
     * {@code "allocatedDays"} fields. Missing optional fields ({@code description},
     * {@code dependencies}) are filled with safe defaults before the object is accepted.
     *
     * @param raw The raw Claude response string.
     * @return A {@link JSONArray} of successfully extracted milestone objects,
     *         or {@code null} if no valid blocks were found.
     */
    private static JSONArray attemptRegexExtraction(String raw) {
        Pattern blockPattern = Pattern.compile(
                "\\{[^{}]*\"name\"[^{}]*\"allocatedDays\"[^{}]*\\}",
                Pattern.DOTALL
        );
        Matcher matcher = blockPattern.matcher(raw);
        JSONArray result = new JSONArray();

        while (matcher.find()) {
            String block = matcher.group();
            try {
                JSONObject obj = new JSONObject(block);
                if (obj.has("name") && obj.has("allocatedDays")) {
                    if (!obj.has("description")) obj.put("description", "");
                    if (!obj.has("dependencies")) obj.put("dependencies", new JSONArray());
                    result.put(obj);
                }
            } catch (JSONException e) {
                Log.w(TAG, "attemptRegexExtraction — block parse failed: " + e.getMessage());
            }
        }

        return result.length() > 0 ? result : null;
    }

    /**
     * Converts a parsed JSON array of milestone objects into a list of
     * {@link MatrixMilestone} instances with sequential ordering and cumulative start days.
     *
     * <p>{@code startDay} for each milestone is computed as the running sum of
     * {@code allocatedDays} from all preceding milestones, so milestones chain
     * end-to-end with no gaps. {@code allocatedDays} is clamped to a minimum of 1
     * to prevent zero-length milestones from entering the data layer.
     *
     * <p>Milestones that fail JSON extraction are skipped and logged; the remaining
     * valid entries are still returned, preserving as much of Claude's output as possible.
     *
     * @param arr A {@link JSONArray} of raw milestone objects from Claude's response.
     * @return An ordered list of {@link MatrixMilestone} objects, potentially empty
     *         if all entries were malformed.
     */
    private static List<MatrixMilestone> buildMilestones(JSONArray arr) {
        List<MatrixMilestone> milestones = new ArrayList<>();
        int cumulativeStartDay = 0;

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                String name = obj.optString("name", "Milestone " + (i + 1));
                String description = obj.optString("description", "");
                int allocatedDays = Math.max(1, obj.optInt("allocatedDays", 1));

                MatrixMilestone ms = new MatrixMilestone(
                        null,
                        null,
                        name,
                        description,
                        i,
                        allocatedDays,
                        0,
                        cumulativeStartDay
                );

                milestones.add(ms);
                cumulativeStartDay += allocatedDays;

            } catch (JSONException e) {
                Log.w(TAG, "buildMilestones — skipping milestone at index " + i + ": " + e.getMessage());
            }
        }

        return milestones;
    }
}

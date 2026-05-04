package com.example.iaso.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// MatrixStorage is the only class allowed to read/write goal data to the device.
// Everything is saved as JSON strings inside Android's SharedPreferences (a simple key-value store on disk).
// No external database is used — just two keys: one holds all goals, one holds all snapshots.
public final class MatrixStorage {

    private static final String TAG = "MatrixStorage";

    // The name of the SharedPreferences file on disk where all matrix data lives.
    private static final String PREFS_FILE = "MatrixData";

    // The key under which the full list of goals (as a JSON array string) is stored.
    private static final String KEY_GOALS = "matrixGoals";

    // The key under which all progress snapshots (as a JSON object string) are stored.
    private static final String KEY_SNAPSHOTS = "matrixSnapshots";

    // The old SharedPreferences file name from before this system existed — used only for migration.
    private static final String LEGACY_PREFS_FILE = "HabitMilestones";

    // A flag stored in prefs so we only run the migration from the old format once.
    private static final String KEY_MIGRATED = "legacyMigrationDone";

    // A lock object used to prevent two threads from reading/writing at the same time.
    private static final Object LOCK = new Object();

    // Private constructor — this class is never instantiated, all methods are static.
    private MatrixStorage() {
    }

    // -----------------------------------------------------------------------
    // PUBLIC API — Goal operations
    // -----------------------------------------------------------------------

    // Saves a goal to disk. If a goal with the same ID already exists, it is replaced.
    // If it's brand new, it's appended to the list.
    public static void saveGoal(Context context, MatrixGoal goal) {
        if (context == null || goal == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);

            JSONObject incomingGoalJson;
            try {
                incomingGoalJson = goalToJson(goal);
            } catch (JSONException e) {
                Log.e(TAG, "saveGoal: failed to serialize goal " + goal.getGoalId(), e);
                return;
            }

            // Walk the existing list and replace if we find a matching ID.
            boolean replaced = false;
            String incomingGoalId = safeStr(goal.getGoalId());

            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject existing = goals.getJSONObject(i);
                    if (incomingGoalId.equals(existing.optString("goalId"))) {
                        goals.put(i, incomingGoalJson);
                        replaced = true;
                        break;
                    }
                } catch (JSONException ignored) {
                }
            }

            // If no existing entry was replaced, add it as a new entry.
            if (!replaced) goals.put(incomingGoalJson);
            writeGoalsArray(prefs, goals);
        }
    }

    // Finds and returns a single goal by its unique ID. Returns null if not found.
    public static MatrixGoal getGoal(Context context, String goalId) {
        if (context == null || goalId == null) return null;
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONArray goals = readGoalsArray(getPrefs(context));
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (goalId.equals(obj.optString("goalId"))) return goalFromJson(obj);
                } catch (JSONException ignored) {
                }
            }
            return null;
        }
    }

    // Looks up a goal by its habit name instead of its ID. Useful when you know the name but not the ID.
    public static MatrixGoal getGoalByHabitName(Context context, String habitName) {
        if (context == null || habitName == null) return null;
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONArray goals = readGoalsArray(getPrefs(context));
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (habitName.equals(obj.optString("habitName"))) return goalFromJson(obj);
                } catch (JSONException ignored) {
                }
            }
            return null;
        }
    }

    // Returns every saved goal as a list. Returns an empty list if nothing is saved yet.
    public static ArrayList<MatrixGoal> getAllGoals(Context context) {
        ArrayList<MatrixGoal> result = new ArrayList<>();
        if (context == null) return result;
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONArray goals = readGoalsArray(getPrefs(context));
            for (int i = 0; i < goals.length(); i++) {
                try {
                    result.add(goalFromJson(goals.getJSONObject(i)));
                } catch (JSONException ignored) {
                }
            }
        }
        return result;
    }

    // Removes a goal and its associated snapshot from disk permanently.
    public static void deleteGoal(Context context, String goalId) {
        if (context == null || goalId == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);

            // Build a new array that contains everything except the goal being deleted.
            JSONArray updated = new JSONArray();
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (!goalId.equals(obj.optString("goalId"))) updated.put(obj);
                } catch (JSONException ignored) {
                }
            }

            // Also remove the snapshot for this goal so no orphaned progress data is left behind.
            JSONObject snapshots = readSnapshotsObject(prefs);
            snapshots.remove(goalId);

            prefs.edit()
                    .putString(KEY_GOALS, updated.toString())
                    .putString(KEY_SNAPSHOTS, snapshots.toString())
                    .apply();
        }
    }

    // -----------------------------------------------------------------------
    // PUBLIC API — Milestone operations
    // -----------------------------------------------------------------------

    // Replaces the entire milestones list for a specific goal.
    // Use this when setting up or bulk-updating milestones.
    public static void saveMilestones(Context context, String goalId, List<MatrixMilestone> milestones) {
        if (context == null || goalId == null || milestones == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);

            // Find the matching goal and overwrite its milestones field.
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (goalId.equals(obj.optString("goalId"))) {
                        obj.put("milestones", milestonesToJson(milestones));
                        goals.put(i, obj);
                        break;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "saveMilestones: failed to serialize milestones for goal " + goalId, e);
                }
            }

            writeGoalsArray(prefs, goals);
        }
    }

    // Returns the list of milestones for a given goal. Returns an empty list if the goal has none.
    public static List<MatrixMilestone> getMilestones(Context context, String goalId) {
        if (context == null || goalId == null) return new ArrayList<>();
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONArray goals = readGoalsArray(getPrefs(context));
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (goalId.equals(obj.optString("goalId"))) {
                        JSONArray arr = obj.optJSONArray("milestones");
                        return arr != null ? milestonesFromJson(arr) : new ArrayList<MatrixMilestone>();
                    }
                } catch (JSONException ignored) {
                }
            }
            return new ArrayList<>();
        }
    }

    // Updates the status of a single milestone (e.g. PENDING → ACTIVE) without rewriting the whole goal.
    // Searches every goal's milestone list until it finds the matching milestoneId.
    public static void updateMilestoneStatus(Context context, String milestoneId, MatrixMilestone.Status newStatus) {
        if (context == null || milestoneId == null || newStatus == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);
            boolean changed = false;

            // Outer loop: iterate over goals. Inner loop: iterate over each goal's milestones.
            // "break outer" exits both loops at once once the target milestone is found.
            outer:
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject goalObj = goals.getJSONObject(i);
                    JSONArray milestones = goalObj.optJSONArray("milestones");
                    if (milestones == null) continue;

                    for (int j = 0; j < milestones.length(); j++) {
                        JSONObject ms = milestones.getJSONObject(j);
                        if (milestoneId.equals(ms.optString("milestoneId"))) {
                            ms.put("status", newStatus.name());
                            milestones.put(j, ms);
                            goalObj.put("milestones", milestones);
                            goals.put(i, goalObj);
                            changed = true;
                            break outer;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "updateMilestoneStatus: failed for milestone " + milestoneId, e);
                }
            }

            // Only write to disk if something actually changed.
            if (changed) writeGoalsArray(prefs, goals);
        }
    }

    // -----------------------------------------------------------------------
    // PUBLIC API — Daily task slot operations
    // -----------------------------------------------------------------------

    // Saves or updates a single daily task slot inside a milestone.
    // If the slot already exists (matched by slotId), it is replaced. Otherwise it is added.
    public static void updateDailyTaskSlot(Context context, String milestoneId, MatrixDailyTaskSlot slot) {
        if (context == null || milestoneId == null || slot == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);
            boolean changed = false;

            // Three nested loops: goals → milestones → slots.
            // "break outer" exits all three once the work is done.
            outer:
            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject goalObj = goals.getJSONObject(i);
                    JSONArray milestones = goalObj.optJSONArray("milestones");
                    if (milestones == null) continue;

                    for (int j = 0; j < milestones.length(); j++) {
                        JSONObject ms = milestones.getJSONObject(j);
                        if (!milestoneId.equals(ms.optString("milestoneId"))) continue;

                        JSONArray slots = ms.optJSONArray("dailyTaskSlots");
                        if (slots == null) slots = new JSONArray();

                        // Check if this slot already exists; if so replace it, otherwise append it.
                        boolean slotFound = false;
                        for (int k = 0; k < slots.length(); k++) {
                            JSONObject existingSlot = slots.getJSONObject(k);
                            if (safeStr(slot.getSlotId()).equals(existingSlot.optString("slotId"))) {
                                slots.put(k, taskSlotToJson(slot));
                                slotFound = true;
                                break;
                            }
                        }

                        if (!slotFound) slots.put(taskSlotToJson(slot));

                        ms.put("dailyTaskSlots", slots);
                        milestones.put(j, ms);
                        goalObj.put("milestones", milestones);
                        goals.put(i, goalObj);
                        changed = true;
                        break outer;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "updateDailyTaskSlot: failed for milestone " + milestoneId, e);
                }
            }

            if (changed) writeGoalsArray(prefs, goals);
        }
    }

    // -----------------------------------------------------------------------
    // PUBLIC API — Snapshot operations
    // -----------------------------------------------------------------------

    // Saves a progress snapshot for a goal. Snapshots are stored separately from goals
    // (in their own JSON object keyed by goalId) so they can be updated frequently without
    // rewriting the entire goals list.
    public static void saveSnapshot(Context context, MatrixSnapshot snapshot) {
        if (context == null || snapshot == null || snapshot.getActiveGoalId() == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONObject snapshots = readSnapshotsObject(prefs);

            try {
                snapshots.put(snapshot.getActiveGoalId(), snapshotToJson(snapshot));
                prefs.edit().putString(KEY_SNAPSHOTS, snapshots.toString()).apply();
            } catch (JSONException e) {
                Log.e(TAG, "saveSnapshot: failed to serialize snapshot for goal " + snapshot.getActiveGoalId(), e);
            }
        }
    }

    // Returns the progress snapshot for a goal, or null if none has been saved yet.
    public static MatrixSnapshot getSnapshot(Context context, String goalId) {
        if (context == null || goalId == null) return null;
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONObject snapshots = readSnapshotsObject(getPrefs(context));
            JSONObject obj = snapshots.optJSONObject(goalId);
            return obj != null ? snapshotFromJson(obj) : null;
        }
    }

    // -----------------------------------------------------------------------
    // MIGRATION — Runs once to convert old HabitMilestones data to the new format
    // -----------------------------------------------------------------------

    // Called at the start of every public method. Checks if migration has already run;
    // if not, it reads the old storage format and converts each habit into a MatrixGoal.
    private static void ensureMigrated(Context context) {
        SharedPreferences matrixPrefs = getPrefs(context);

        // Fast path: migration already done, do nothing.
        if (matrixPrefs.getBoolean(KEY_MIGRATED, false)) return;

        synchronized (LOCK) {
            // Double-check inside the lock in case another thread just finished migrating.
            if (matrixPrefs.getBoolean(KEY_MIGRATED, false)) return;

            SharedPreferences legacyPrefs =
                    context.getSharedPreferences(LEGACY_PREFS_FILE, Context.MODE_PRIVATE);

            JSONArray currentGoals = readGoalsArray(matrixPrefs);

            String legacyListJson = legacyPrefs.getString("habitMilestonesList", null);
            if (legacyListJson != null) {
                try {
                    JSONArray habitMilestonesList = new JSONArray(legacyListJson);
                    for (int idx = 0; idx < habitMilestonesList.length(); idx++) {
                        JSONObject habitMilestonesObj = habitMilestonesList.getJSONObject(idx);
                        String habitName = habitMilestonesObj.optString("habitName", "");
                        if (habitName.isEmpty()) continue;

                        JSONArray legacyMilestones = habitMilestonesObj.optJSONArray("milestones");
                        if (legacyMilestones == null) legacyMilestones = new JSONArray();

                        // Skip this habit if a goal with the same name was already migrated.
                        boolean goalExists = false;
                        for (int i = 0; i < currentGoals.length(); i++) {
                            JSONObject g = currentGoals.getJSONObject(i);
                            if (habitName.equals(g.optString("habitName"))) {
                                goalExists = true;
                                break;
                            }
                        }

                        if (!goalExists) {
                            // Create a shell MatrixGoal to wrap the old habit data.
                            MatrixGoal syntheticGoal = new MatrixGoal(
                                    habitName,
                                    "Migrated from legacy storage",
                                    0,
                                    System.currentTimeMillis()
                            );

                            JSONObject goalJson = goalToJson(syntheticGoal);
                            List<MatrixMilestone> milestones =
                                    migrateLegacyMilestones(legacyMilestones, syntheticGoal.getGoalId());
                            goalJson.put("milestones", milestonesToJson(milestones));
                            currentGoals.put(goalJson);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "ensureMigrated: failed to parse legacy milestones list", e);
                }
            }

            // Save the converted data and mark migration as done so this never runs again.
            matrixPrefs.edit()
                    .putString(KEY_GOALS, currentGoals.toString())
                    .putBoolean(KEY_MIGRATED, true)
                    .apply();
        }
    }

    // Converts old-format milestone JSON objects into MatrixMilestone objects.
    // The old format only had name, time, and days — no IDs or statuses.
    // startDay is calculated cumulatively so milestones chain end-to-end.
    private static List<MatrixMilestone> migrateLegacyMilestones(JSONArray legacyArray, String parentGoalId) {
        List<MatrixMilestone> result = new ArrayList<>();
        int cumulativeStartDay = 0;

        for (int i = 0; i < legacyArray.length(); i++) {
            try {
                JSONObject obj = legacyArray.getJSONObject(i);

                String name = obj.optString("name", "Migrated Milestone");
                String time = obj.optString("time", "");
                int days = Math.max(0, obj.optInt("days", 0));

                MatrixMilestone ms = new MatrixMilestone();
                ms.setParentGoalId(parentGoalId);
                ms.setName(name);
                ms.setDescription(time);
                ms.setOrderIndex(i);
                ms.setAllocatedDays(days);
                ms.setBufferDays(0);
                ms.setStartDay(cumulativeStartDay);

                result.add(ms);
                cumulativeStartDay += days; // next milestone starts right after this one ends
            } catch (JSONException ignored) {
            }
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // SERIALIZATION — Converting Java objects → JSON and JSON → Java objects
    // -----------------------------------------------------------------------

    // Converts a MatrixGoal object into a JSON object so it can be stored as a string.
    private static JSONObject goalToJson(MatrixGoal g) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("goalId", safeStr(g.getGoalId()));
        obj.put("habitName", safeStr(g.getHabitName()));
        obj.put("goalDescription", safeStr(g.getGoalDescription()));
        obj.put("totalDays", g.getTotalDays());
        obj.put("bufferDays", g.getBufferDays());
        obj.put("startDate", g.getStartDate());
        obj.put("targetDate", g.getTargetDate());
        obj.put("status", g.getStatus() != null ? g.getStatus().name() : MatrixGoal.Status.ACTIVE.name());
        obj.put("createdAt", g.getCreatedAt());

        List<MatrixMilestone> milestones = g.getMilestones();
        if (milestones == null) milestones = new ArrayList<>();
        obj.put("milestones", milestonesToJson(milestones));

        return obj;
    }

    // Rebuilds a MatrixGoal Java object from a JSON object read off disk.
    // Uses safe defaults (e.g. 0 for missing numbers) so corrupt/partial data doesn't crash the app.
    private static MatrixGoal goalFromJson(JSONObject obj) {
        MatrixGoal g = new MatrixGoal();
        g.setGoalId(obj.optString("goalId"));
        g.setHabitName(obj.optString("habitName"));
        g.setGoalDescription(obj.optString("goalDescription"));

        int totalDays = Math.max(0, obj.optInt("totalDays", 0));
        long now = System.currentTimeMillis();
        long startDate = nonNegativeLong(obj.optLong("startDate", now), now);

        g.setStartDate(startDate);
        g.setTotalDays(totalDays);
        g.setBufferDays(Math.max(0, obj.optInt("bufferDays", 0)));
        g.setTargetDate(Math.max(0L, obj.optLong("targetDate", 0L)));
        g.setCreatedAt(nonNegativeLong(obj.optLong("createdAt", now), now));

        // Parse the status enum from its string name; fall back to ACTIVE if unrecognised.
        String statusStr = obj.optString("status", MatrixGoal.Status.ACTIVE.name());
        try {
            g.setStatus(MatrixGoal.Status.valueOf(statusStr));
        } catch (IllegalArgumentException ignored) {
            g.setStatus(MatrixGoal.Status.ACTIVE);
        }

        JSONArray milestonesArray = obj.optJSONArray("milestones");
        if (milestonesArray != null) g.setMilestones(milestonesFromJson(milestonesArray));
        else g.setMilestones(new ArrayList<MatrixMilestone>());

        return g;
    }

    // Converts a list of milestones into a JSON array by serializing each one.
    private static JSONArray milestonesToJson(List<MatrixMilestone> milestones) throws JSONException {
        JSONArray arr = new JSONArray();
        for (MatrixMilestone ms : milestones) arr.put(milestoneToJson(ms));
        return arr;
    }

    // Converts a single MatrixMilestone into a JSON object.
    private static JSONObject milestoneToJson(MatrixMilestone ms) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("milestoneId", safeStr(ms.getMilestoneId()));
        obj.put("parentGoalId", safeStr(ms.getParentGoalId()));
        obj.put("name", safeStr(ms.getName()));
        obj.put("description", safeStr(ms.getDescription()));
        obj.put("orderIndex", Math.max(0, ms.getOrderIndex()));
        obj.put("allocatedDays", Math.max(0, ms.getAllocatedDays()));
        obj.put("bufferDays", Math.max(0, ms.getBufferDays()));
        obj.put("startDay", Math.max(0, ms.getStartDay()));
        obj.put("status", ms.getStatus() != null ? ms.getStatus().name() : MatrixMilestone.Status.PENDING.name());

        List<MatrixDailyTaskSlot> slots = ms.getDailyTaskSlots();
        if (slots == null) slots = new ArrayList<>();
        obj.put("dailyTaskSlots", taskSlotsToJson(slots));

        return obj;
    }

    // Rebuilds a list of MatrixMilestone objects from a JSON array read off disk.
    private static List<MatrixMilestone> milestonesFromJson(JSONArray arr) {
        List<MatrixMilestone> list = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                String milestoneId = obj.optString("milestoneId");
                String parentGoalId = obj.optString("parentGoalId");
                String name = obj.optString("name");
                String description = obj.optString("description");
                int orderIndex = Math.max(0, obj.optInt("orderIndex", 0));
                int allocatedDays = Math.max(0, obj.optInt("allocatedDays", 0));
                int bufferDays = Math.max(0, obj.optInt("bufferDays", 0));
                int startDay = Math.max(0, obj.optInt("startDay", 0));

                MatrixMilestone ms = new MatrixMilestone(
                        milestoneId,
                        parentGoalId,
                        name,
                        description,
                        orderIndex,
                        allocatedDays,
                        bufferDays,
                        startDay
                );

                // Parse the status enum from its string name; fall back to PENDING if unrecognised.
                String statusStr = obj.optString("status", MatrixMilestone.Status.PENDING.name());
                try {
                    ms.setStatus(MatrixMilestone.Status.valueOf(statusStr));
                } catch (IllegalArgumentException ignored) {
                    ms.setStatus(MatrixMilestone.Status.PENDING);
                }

                JSONArray slots = obj.optJSONArray("dailyTaskSlots");
                if (slots != null) ms.setDailyTaskSlots(taskSlotsFromJson(slots));

                list.add(ms);
            } catch (JSONException ignored) {
            }
        }

        return list;
    }

    // Converts a list of daily task slots into a JSON array.
    private static JSONArray taskSlotsToJson(List<MatrixDailyTaskSlot> slots) throws JSONException {
        JSONArray arr = new JSONArray();
        for (MatrixDailyTaskSlot slot : slots) arr.put(taskSlotToJson(slot));
        return arr;
    }

    // Converts a single MatrixDailyTaskSlot into a JSON object.
    private static JSONObject taskSlotToJson(MatrixDailyTaskSlot slot) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("slotId", safeStr(slot.getSlotId()));
        obj.put("parentMilestoneId", safeStr(slot.getParentMilestoneId()));
        obj.put("dayNumber", Math.max(1, slot.getDayNumber()));
        obj.put("date", Math.max(0L, slot.getDate()));
        obj.put("status", slot.getStatus() != null ? slot.getStatus().name() : MatrixDailyTaskSlot.Status.PENDING.name());
        obj.put("taskPayload", slot.getTaskPayload() != null ? slot.getTaskPayload() : "");
        return obj;
    }

    // Rebuilds a list of MatrixDailyTaskSlot objects from a JSON array read off disk.
    private static List<MatrixDailyTaskSlot> taskSlotsFromJson(JSONArray arr) {
        List<MatrixDailyTaskSlot> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                list.add(taskSlotFromJson(arr.getJSONObject(i)));
            } catch (JSONException ignored) {
            }
        }
        return list;
    }

    // Rebuilds a single MatrixDailyTaskSlot from a JSON object.
    private static MatrixDailyTaskSlot taskSlotFromJson(JSONObject obj) {
        MatrixDailyTaskSlot slot = new MatrixDailyTaskSlot();
        slot.setSlotId(obj.optString("slotId"));
        slot.setParentMilestoneId(obj.optString("parentMilestoneId"));
        slot.setDayNumber(Math.max(1, obj.optInt("dayNumber", 1)));
        slot.setDate(Math.max(0L, obj.optLong("date", 0L)));

        // Parse the status enum from its string name; fall back to PENDING if unrecognised.
        String statusStr = obj.optString("status", MatrixDailyTaskSlot.Status.PENDING.name());
        try {
            slot.setStatus(MatrixDailyTaskSlot.Status.valueOf(statusStr));
        } catch (IllegalArgumentException ignored) {
            slot.setStatus(MatrixDailyTaskSlot.Status.PENDING);
        }

        slot.setTaskPayload(obj.optString("taskPayload", ""));
        return slot;
    }

    // Converts a MatrixSnapshot into a JSON object.
    private static JSONObject snapshotToJson(MatrixSnapshot s) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("activeGoalId", safeStr(s.getActiveGoalId()));
        obj.put("activeMilestoneIndex", Math.max(0, s.getActiveMilestoneIndex()));
        obj.put("currentDayInMilestone", Math.max(1, s.getCurrentDayInMilestone()));
        obj.put("totalDaysElapsed", Math.max(0, s.getTotalDaysElapsed()));
        obj.put("totalDaysRemaining", Math.max(0, s.getTotalDaysRemaining()));
        obj.put("bufferRemaining", Math.max(0, s.getBufferRemaining()));
        obj.put("lastUpdated", Math.max(0L, s.getLastUpdated()));
        return obj;
    }

    // Rebuilds a MatrixSnapshot from a JSON object read off disk.
    private static MatrixSnapshot snapshotFromJson(JSONObject obj) {
        String activeGoalId = obj.optString("activeGoalId");
        int activeMilestoneIndex = Math.max(0, obj.optInt("activeMilestoneIndex", 0));
        int currentDayInMilestone = Math.max(1, obj.optInt("currentDayInMilestone", 1));
        int totalDaysElapsed = Math.max(0, obj.optInt("totalDaysElapsed", 0));
        int totalDaysRemaining = Math.max(0, obj.optInt("totalDaysRemaining", 0));
        int bufferRemaining = Math.max(0, obj.optInt("bufferRemaining", 0));
        long lastUpdated = Math.max(0L, obj.optLong("lastUpdated", System.currentTimeMillis()));

        MatrixSnapshot snapshot = new MatrixSnapshot(
                activeGoalId,
                activeMilestoneIndex,
                currentDayInMilestone,
                totalDaysElapsed,
                totalDaysRemaining,
                bufferRemaining
        );
        snapshot.setLastUpdated(lastUpdated);
        return snapshot;
    }

    // -----------------------------------------------------------------------
    // HELPERS — Low-level read/write utilities
    // -----------------------------------------------------------------------

    // Opens (or creates) the SharedPreferences file where all matrix data is stored.
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    // Reads the raw goals JSON string from disk and parses it into a JSONArray.
    // Returns an empty array if nothing is saved yet or the data is corrupted.
    private static JSONArray readGoalsArray(SharedPreferences prefs) {
        String raw = prefs.getString(KEY_GOALS, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // Writes the goals JSON array back to disk as a string.
    private static void writeGoalsArray(SharedPreferences prefs, JSONArray goals) {
        prefs.edit().putString(KEY_GOALS, goals.toString()).apply();
    }

    // Reads the raw snapshots JSON string from disk and parses it into a JSONObject.
    // Returns an empty object if nothing is saved yet or the data is corrupted.
    private static JSONObject readSnapshotsObject(SharedPreferences prefs) {
        String raw = prefs.getString(KEY_SNAPSHOTS, "{}");
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    // Returns an empty string instead of null so JSON serialization never writes "null" for string fields.
    private static String safeStr(String s) {
        return s != null ? s : "";
    }

    // Returns the value if it's non-negative, otherwise returns the provided default.
    // Used when reading timestamps from disk to guard against corrupted negative values.
    private static long nonNegativeLong(long value, long defaultValue) {
        return value >= 0L ? value : defaultValue;
    }
}

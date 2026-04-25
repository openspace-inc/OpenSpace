package com.example.iaso.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MatrixStorage {

    private static final String TAG = "MatrixStorage";
    private static final String PREFS_FILE = "MatrixData";
    private static final String KEY_GOALS = "matrixGoals";
    private static final String KEY_SNAPSHOTS = "matrixSnapshots";
    private static final String LEGACY_PREFS_FILE = "HabitMilestones";
    private static final String KEY_MIGRATED = "legacyMigrationDone";

    private static final Object LOCK = new Object();

    private MatrixStorage() {
    }

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

            if (!replaced) goals.put(incomingGoalJson);
            writeGoalsArray(prefs, goals);
        }
    }

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

    public static void deleteGoal(Context context, String goalId) {
        if (context == null || goalId == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);
            JSONArray updated = new JSONArray();

            for (int i = 0; i < goals.length(); i++) {
                try {
                    JSONObject obj = goals.getJSONObject(i);
                    if (!goalId.equals(obj.optString("goalId"))) updated.put(obj);
                } catch (JSONException ignored) {
                }
            }

            JSONObject snapshots = readSnapshotsObject(prefs);
            snapshots.remove(goalId);

            prefs.edit()
                    .putString(KEY_GOALS, updated.toString())
                    .putString(KEY_SNAPSHOTS, snapshots.toString())
                    .apply();
        }
    }

    public static void saveMilestones(Context context, String goalId, List<MatrixMilestone> milestones) {
        if (context == null || goalId == null || milestones == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);

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

    public static void updateMilestoneStatus(Context context, String milestoneId, MatrixMilestone.Status newStatus) {
        if (context == null || milestoneId == null || newStatus == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);
            boolean changed = false;

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
                } catch (JSONException ignored) {
                }
            }

            if (changed) writeGoalsArray(prefs, goals);
        }
    }

    public static void updateDailyTaskSlot(Context context, String milestoneId, MatrixDailyTaskSlot slot) {
        if (context == null || milestoneId == null || slot == null) return;
        ensureMigrated(context);

        synchronized (LOCK) {
            SharedPreferences prefs = getPrefs(context);
            JSONArray goals = readGoalsArray(prefs);
            boolean changed = false;

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

    public static MatrixSnapshot getSnapshot(Context context, String goalId) {
        if (context == null || goalId == null) return null;
        ensureMigrated(context);

        synchronized (LOCK) {
            JSONObject snapshots = readSnapshotsObject(getPrefs(context));
            JSONObject obj = snapshots.optJSONObject(goalId);
            return obj != null ? snapshotFromJson(obj) : null;
        }
    }

    private static void ensureMigrated(Context context) {
        SharedPreferences matrixPrefs = getPrefs(context);

        if (matrixPrefs.getBoolean(KEY_MIGRATED, false)) return;

        synchronized (LOCK) {
            if (matrixPrefs.getBoolean(KEY_MIGRATED, false)) return;

            SharedPreferences legacyPrefs =
                    context.getSharedPreferences(LEGACY_PREFS_FILE, Context.MODE_PRIVATE);

            JSONArray currentGoals = readGoalsArray(matrixPrefs);

            for (Map.Entry<String, ?> entry : legacyPrefs.getAll().entrySet()) {
                String habitName = entry.getKey();
                Object rawValue = entry.getValue();
                if (!(rawValue instanceof String)) continue;

                try {
                    JSONArray legacyMilestones = new JSONArray((String) rawValue);

                    boolean goalExists = false;
                    for (int i = 0; i < currentGoals.length(); i++) {
                        JSONObject g = currentGoals.getJSONObject(i);
                        if (habitName.equals(g.optString("habitName"))) {
                            goalExists = true;
                            break;
                        }
                    }

                    if (!goalExists) {
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

                } catch (JSONException e) {
                    Log.e(TAG, "ensureMigrated: failed to migrate habit " + habitName, e);
                }
            }

            matrixPrefs.edit()
                    .putString(KEY_GOALS, currentGoals.toString())
                    .putBoolean(KEY_MIGRATED, true)
                    .apply();
        }
    }

    private static List<MatrixMilestone> migrateLegacyMilestones(JSONArray legacyArray, String parentGoalId) {
        List<MatrixMilestone> result = new ArrayList<>();

        for (int i = 0; i < legacyArray.length(); i++) {
            try {
                JSONObject obj = legacyArray.getJSONObject(i);

                MatrixMilestone ms = new MatrixMilestone();

                String storedId = obj.optString("milestoneId", "");
                if (!storedId.isEmpty()) ms.setMilestoneId(storedId);

                ms.setParentGoalId(parentGoalId);
                ms.setName(obj.optString("name", "Migrated Milestone"));
                ms.setDescription(obj.optString("description", ""));
                ms.setOrderIndex(Math.max(0, obj.optInt("orderIndex", i)));
                ms.setAllocatedDays(Math.max(0, obj.optInt("allocatedDays", 0)));
                ms.setBufferDays(Math.max(0, obj.optInt("bufferDays", 0)));
                ms.setStartDay(Math.max(0, obj.optInt("startDay", 0)));

                String statusStr = obj.optString("status", MatrixMilestone.Status.PENDING.name());
                try {
                    ms.setStatus(MatrixMilestone.Status.valueOf(statusStr));
                } catch (IllegalArgumentException ignored) {
                    ms.setStatus(MatrixMilestone.Status.PENDING);
                }

                result.add(ms);
            } catch (JSONException ignored) {
            }
        }

        return result;
    }

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

    private static JSONArray milestonesToJson(List<MatrixMilestone> milestones) throws JSONException {
        JSONArray arr = new JSONArray();
        for (MatrixMilestone ms : milestones) arr.put(milestoneToJson(ms));
        return arr;
    }

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

    private static JSONArray taskSlotsToJson(List<MatrixDailyTaskSlot> slots) throws JSONException {
        JSONArray arr = new JSONArray();
        for (MatrixDailyTaskSlot slot : slots) arr.put(taskSlotToJson(slot));
        return arr;
    }

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

    private static MatrixDailyTaskSlot taskSlotFromJson(JSONObject obj) {
        MatrixDailyTaskSlot slot = new MatrixDailyTaskSlot();
        slot.setSlotId(obj.optString("slotId"));
        slot.setParentMilestoneId(obj.optString("parentMilestoneId"));
        slot.setDayNumber(Math.max(1, obj.optInt("dayNumber", 1)));
        slot.setDate(Math.max(0L, obj.optLong("date", 0L)));

        String statusStr = obj.optString("status", MatrixDailyTaskSlot.Status.PENDING.name());
        try {
            slot.setStatus(MatrixDailyTaskSlot.Status.valueOf(statusStr));
        } catch (IllegalArgumentException ignored) {
            slot.setStatus(MatrixDailyTaskSlot.Status.PENDING);
        }

        slot.setTaskPayload(obj.optString("taskPayload", ""));
        return slot;
    }

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

    private static MatrixSnapshot snapshotFromJson(JSONObject obj) {
        String activeGoalId = obj.optString("activeGoalId");
        int activeMilestoneIndex = Math.max(0, obj.optInt("activeMilestoneIndex", 0));
        int currentDayInMilestone = Math.max(1, obj.optInt("currentDayInMilestone", 1));
        int totalDaysElapsed = Math.max(0, obj.optInt("totalDaysElapsed", 0));
        int totalDaysRemaining = Math.max(0, obj.optInt("totalDaysRemaining", 0));
        int bufferRemaining = Math.max(0, obj.optInt("bufferRemaining", 0));
        long lastUpdated = Math.max(0L, obj.optLong("lastUpdated", System.currentTimeMillis()));

        return new MatrixSnapshot(
                activeGoalId,
                activeMilestoneIndex,
                currentDayInMilestone,
                totalDaysElapsed,
                totalDaysRemaining,
                bufferRemaining,
                lastUpdated
        );
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    private static JSONArray readGoalsArray(SharedPreferences prefs) {
        String raw = prefs.getString(KEY_GOALS, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private static void writeGoalsArray(SharedPreferences prefs, JSONArray goals) {
        prefs.edit().putString(KEY_GOALS, goals.toString()).apply();
    }

    private static JSONObject readSnapshotsObject(SharedPreferences prefs) {
        String raw = prefs.getString(KEY_SNAPSHOTS, "{}");
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private static String safeStr(String s) {
        return s != null ? s : "";
    }

    private static long nonNegativeLong(long value, long defaultValue) {
        return value >= 0L ? value : defaultValue;
    }
}
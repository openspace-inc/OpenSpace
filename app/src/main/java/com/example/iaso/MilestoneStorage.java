package com.example.iaso;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for storing and retrieving milestone data from SharedPreferences.
 *
 * This class provides a clean interface for:
 * - Saving milestone lists associated with habit names
 * - Retrieving milestones for a specific habit
 * - Getting all stored habit milestones
 *
 * STORAGE STRUCTURE:
 * SharedPreferences: "HabitMilestones"
 * Key: "habitMilestonesList"
 * Value: JSON array of HabitMilestones objects
 */
public class MilestoneStorage {

    /** SharedPreferences file name */
    private static final String PREFS_NAME = "HabitMilestones";

    /** Key for storing the milestone list */
    private static final String KEY_MILESTONE_LIST = "habitMilestonesList";

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    /**
     * Constructor
     * @param context Application context
     */
    public MilestoneStorage(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Saves milestones for a specific habit.
     * If milestones for this habit already exist, they will be replaced.
     *
     * @param habitName The name/ticker symbol of the habit
     * @param milestones List of milestones to save
     */
    public void saveMilestones(String habitName, List<Milestone> milestones) {
        // Load existing list
        List<HabitMilestones> allHabitMilestones = getAllHabitMilestones();

        // Remove existing entry for this habit if it exists
        allHabitMilestones.removeIf(hm -> hm.getHabitName().equals(habitName));

        // Add new entry
        HabitMilestones newHabitMilestones = new HabitMilestones(habitName, milestones);
        allHabitMilestones.add(newHabitMilestones);

        // Save back to SharedPreferences
        saveAllHabitMilestones(allHabitMilestones);
    }

    /**
     * Retrieves milestones for a specific habit.
     *
     * @param habitName The name/ticker symbol of the habit
     * @return List of milestones for this habit, or empty list if not found
     */
    public List<Milestone> getMilestonesForHabit(String habitName) {
        List<HabitMilestones> allHabitMilestones = getAllHabitMilestones();

        for (HabitMilestones hm : allHabitMilestones) {
            if (hm.getHabitName().equals(habitName)) {
                return hm.getMilestones();
            }
        }

        // Return empty list if not found
        return new ArrayList<>();
    }

    /**
     * Retrieves all habit milestones from SharedPreferences.
     *
     * @return List of all HabitMilestones objects
     */
    public List<HabitMilestones> getAllHabitMilestones() {
        String json = prefs.getString(KEY_MILESTONE_LIST, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<HabitMilestones>>(){}.getType();
        List<HabitMilestones> list = gson.fromJson(json, type);

        return list != null ? list : new ArrayList<>();
    }

    /**
     * Saves all habit milestones to SharedPreferences.
     *
     * @param allHabitMilestones List of all HabitMilestones to save
     */
    private void saveAllHabitMilestones(List<HabitMilestones> allHabitMilestones) {
        String json = gson.toJson(allHabitMilestones);
        prefs.edit().putString(KEY_MILESTONE_LIST, json).apply();
    }

    /**
     * Deletes milestones for a specific habit.
     *
     * @param habitName The name/ticker symbol of the habit
     * @return true if milestones were deleted, false if not found
     */
    public boolean deleteMilestonesForHabit(String habitName) {
        List<HabitMilestones> allHabitMilestones = getAllHabitMilestones();

        boolean removed = allHabitMilestones.removeIf(hm -> hm.getHabitName().equals(habitName));

        if (removed) {
            saveAllHabitMilestones(allHabitMilestones);
        }

        return removed;
    }

    /**
     * Checks if milestones exist for a specific habit.
     *
     * @param habitName The name/ticker symbol of the habit
     * @return true if milestones exist, false otherwise
     */
    public boolean hasMilestonesForHabit(String habitName) {
        List<HabitMilestones> allHabitMilestones = getAllHabitMilestones();

        for (HabitMilestones hm : allHabitMilestones) {
            if (hm.getHabitName().equals(habitName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clears all stored milestones.
     * Use with caution!
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}

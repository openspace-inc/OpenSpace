package com.example.iaso.FocusTimer;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

/**
 * FocusTimerManager
 *
 * Manages the focus timer state across the application.
 * This class handles:
 * - Starting/stopping/pausing the timer
 * - Persisting timer state to SharedPreferences
 * - Calculating elapsed time correctly even after app restarts
 * - Handling pause state to exclude paused time from calculations
 *
 * Timer State Persistence:
 * The timer state is stored in SharedPreferences under "FocusTimerState" with the following fields:
 * - isActive: boolean indicating if a timer is currently running
 * - habitName: name of the habit being timed
 * - habitImageName: image name for the habit
 * - dailyGoalMinutes: daily time commitment goal
 * - startTimeMillis: timestamp when timer started (System.currentTimeMillis())
 * - isPaused: boolean indicating if timer is paused
 * - pausedTimeMillis: timestamp when timer was paused
 * - totalPausedDurationMillis: total time spent in paused state
 *
 * Thread Safety:
 * This class is designed to be used from the main thread only.
 */
public class FocusTimerManager {
    
    private static final String PREFS_NAME = "FocusTimerState";
    private static final String KEY_IS_ACTIVE = "isActive";
    private static final String KEY_HABIT_NAME = "habitName";
    private static final String KEY_HABIT_IMAGE = "habitImageName";
    private static final String KEY_DAILY_GOAL = "dailyGoalMinutes";
    private static final String KEY_START_TIME = "startTimeMillis";
    private static final String KEY_IS_PAUSED = "isPaused";
    private static final String KEY_PAUSED_TIME = "pausedTimeMillis";
    private static final String KEY_TOTAL_PAUSED = "totalPausedDurationMillis";
    
    private final Context context;
    private final SharedPreferences prefs;
    
    /**
     * Constructor
     * @param context Application context
     */
    public FocusTimerManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Starts a new focus timer for a habit
     * @param habitName Name of the habit
     * @param habitImageName Image name for the habit
     * @param dailyGoalMinutes Daily time commitment goal in minutes
     */
    public void startTimer(String habitName, String habitImageName, int dailyGoalMinutes) {
        long currentTime = System.currentTimeMillis();
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_ACTIVE, true);
        editor.putString(KEY_HABIT_NAME, habitName);
        editor.putString(KEY_HABIT_IMAGE, habitImageName);
        editor.putInt(KEY_DAILY_GOAL, dailyGoalMinutes);
        editor.putLong(KEY_START_TIME, currentTime);
        editor.putBoolean(KEY_IS_PAUSED, false);
        editor.putLong(KEY_PAUSED_TIME, 0);
        editor.putLong(KEY_TOTAL_PAUSED, 0);
        editor.apply();
    }
    
    /**
     * Pauses the currently active timer
     * Does nothing if timer is not active or already paused
     */
    public void pauseTimer() {
        if (!isTimerActive() || isTimerPaused()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_PAUSED, true);
        editor.putLong(KEY_PAUSED_TIME, currentTime);
        editor.apply();
    }
    
    /**
     * Resumes a paused timer
     * Does nothing if timer is not active or not paused
     */
    public void resumeTimer() {
        if (!isTimerActive() || !isTimerPaused()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long pausedTime = prefs.getLong(KEY_PAUSED_TIME, 0);
        long totalPaused = prefs.getLong(KEY_TOTAL_PAUSED, 0);
        
        // Add the duration of this pause to total paused time
        long pauseDuration = currentTime - pausedTime;
        totalPaused += pauseDuration;
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_PAUSED, false);
        editor.putLong(KEY_PAUSED_TIME, 0);
        editor.putLong(KEY_TOTAL_PAUSED, totalPaused);
        editor.apply();
    }
    
    /**
     * Stops the timer and returns the elapsed time
     * @return Elapsed time in minutes (excluding paused time)
     */
    public int stopTimer() {
        if (!isTimerActive()) {
            return 0;
        }
        
        int elapsedMinutes = getElapsedMinutes();
        
        // Clear timer state
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        return elapsedMinutes;
    }
    
    /**
     * Checks if a timer is currently active
     * @return true if timer is active, false otherwise
     */
    public boolean isTimerActive() {
        return prefs.getBoolean(KEY_IS_ACTIVE, false);
    }
    
    /**
     * Checks if the active timer is paused
     * @return true if timer is paused, false otherwise
     */
    public boolean isTimerPaused() {
        return prefs.getBoolean(KEY_IS_PAUSED, false);
    }
    
    /**
     * Gets the name of the habit being timed
     * @return Habit name, or empty string if no active timer
     */
    public String getHabitName() {
        return prefs.getString(KEY_HABIT_NAME, "");
    }
    
    /**
     * Gets the image name of the habit being timed
     * @return Habit image name, or empty string if no active timer
     */
    public String getHabitImageName() {
        return prefs.getString(KEY_HABIT_IMAGE, "");
    }
    
    /**
     * Gets the daily goal in minutes for the habit being timed
     * @return Daily goal in minutes, or 0 if no active timer
     */
    public int getDailyGoalMinutes() {
        return prefs.getInt(KEY_DAILY_GOAL, 0);
    }
    
    /**
     * Gets the elapsed time in minutes (excluding paused time)
     * @return Elapsed minutes since timer started
     */
    public int getElapsedMinutes() {
        if (!isTimerActive()) {
            return 0;
        }
        
        long startTime = prefs.getLong(KEY_START_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long totalPaused = prefs.getLong(KEY_TOTAL_PAUSED, 0);
        
        // If currently paused, add the current pause duration
        if (isTimerPaused()) {
            long pausedTime = prefs.getLong(KEY_PAUSED_TIME, 0);
            long currentPauseDuration = currentTime - pausedTime;
            totalPaused += currentPauseDuration;
        }
        
        // Calculate elapsed time: (current time - start time) - total paused time
        long elapsedMillis = currentTime - startTime - totalPaused;
        
        // Convert to minutes (rounded down)
        return (int) (elapsedMillis / (1000 * 60));
    }
    
    /**
     * Gets the elapsed time in milliseconds (excluding paused time)
     * Used for more precise calculations and UI updates
     * @return Elapsed milliseconds since timer started
     */
    public long getElapsedMillis() {
        if (!isTimerActive()) {
            return 0;
        }
        
        long startTime = prefs.getLong(KEY_START_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long totalPaused = prefs.getLong(KEY_TOTAL_PAUSED, 0);
        
        // If currently paused, add the current pause duration
        if (isTimerPaused()) {
            long pausedTime = prefs.getLong(KEY_PAUSED_TIME, 0);
            long currentPauseDuration = currentTime - pausedTime;
            totalPaused += currentPauseDuration;
        }
        
        // Calculate elapsed time: (current time - start time) - total paused time
        return currentTime - startTime - totalPaused;
    }
    
    /**
     * Gets the progress ratio (0.0 to 1.0) of elapsed time vs daily goal
     * @return Progress ratio, capped at 1.0
     */
    public float getProgressRatio() {
        if (!isTimerActive()) {
            return 0.0f;
        }
        
        int elapsedMinutes = getElapsedMinutes();
        int goalMinutes = getDailyGoalMinutes();
        
        if (goalMinutes <= 0) {
            return 0.0f;
        }
        
        float ratio = (float) elapsedMinutes / (float) goalMinutes;
        return Math.min(ratio, 1.0f); // Cap at 1.0 (100%)
    }
    
    /**
     * Creates a FocusTimerState object representing the current state
     * Useful for passing state to UI components
     * @return FocusTimerState object
     */
    public FocusTimerState getState() {
        return new FocusTimerState(
            isTimerActive(),
            isTimerPaused(),
            getHabitName(),
            getHabitImageName(),
            getDailyGoalMinutes(),
            getElapsedMinutes(),
            getProgressRatio()
        );
    }
    
    /**
     * FocusTimerState
     * 
     * Immutable data class representing the current state of the focus timer
     */
    public static class FocusTimerState {
        public final boolean isActive;
        public final boolean isPaused;
        public final String habitName;
        public final String habitImageName;
        public final int dailyGoalMinutes;
        public final int elapsedMinutes;
        public final float progressRatio;
        
        public FocusTimerState(boolean isActive, boolean isPaused, String habitName, 
                              String habitImageName, int dailyGoalMinutes, 
                              int elapsedMinutes, float progressRatio) {
            this.isActive = isActive;
            this.isPaused = isPaused;
            this.habitName = habitName;
            this.habitImageName = habitImageName;
            this.dailyGoalMinutes = dailyGoalMinutes;
            this.elapsedMinutes = elapsedMinutes;
            this.progressRatio = progressRatio;
        }
    }
}


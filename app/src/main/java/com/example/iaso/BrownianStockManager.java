package com.example.iaso;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.iaso.Model.StockDataPoint;
import com.example.iaso.Model.StockSimulationState;
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.dataStorage;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages the simulated stock prices for each personal project.
 */
public final class BrownianStockManager {

    private static final long INTERVAL_MILLIS = 60L * 60L * 1000L; // 1 hour
    private static final double MIN_PRICE = 0.01d;

    private static final double DAILY_GOAL_BONUS = 0.05d;
    private static final double DAILY_MAX_PENALTY = 0.05d;

    private static final double STABLE_MIN_DELTA = -0.03d;
    private static final double STABLE_MAX_DELTA = 0.04d;
    private static final double UNSTABLE_MIN_DELTA = -0.08d;
    private static final double UNSTABLE_MAX_DELTA = 0.03d;

    private static final String HABIT_PREF = "PersonalHabits";
    private static final String HABIT_LIST_KEY = "personalHabitList";
    private static final String STOCK_HISTORY_KEY = "stockHistory";
    private static final String STOCK_STATE_KEY = "stockSimulationState";

    private static final Random RANDOM = new Random();

    private BrownianStockManager() {
        // Utility class
    }

    /**
     * Updates all saved projects with pending stock ticks up to the current hour.
     */
    public static void updateAll(Context context) {
        if (context == null) {
            return;
        }
        // Grab every saved habit so we can advance each timeline once.
        ArrayList<DynamicHabit> habits = loadHabits(context);
        if (habits == null || habits.isEmpty()) {
            return;
        }

        for (DynamicHabit habit : habits) {
            updateForHabit(context, habit.getName3());
        }
    }

    /**
     * Updates stock ticks for a specific project up to the current hour.
     */
    public static void updateForHabit(Context context, String habitName) {
        if (context == null || habitName == null || habitName.isEmpty()) {
            return;
        }

        // Find the specific habit configuration we are simulating.
        ArrayList<DynamicHabit> habits = loadHabits(context);
        if (habits == null || habits.isEmpty()) {
            return;
        }

        DynamicHabit selectedHabit = null;
        for (DynamicHabit habit : habits) {
            if (habitName.equals(habit.getName3())) {
                selectedHabit = habit;
                break;
            }
        }
        if (selectedHabit == null) {
            return;
        }

        SharedPreferences habitPrefs = context.getSharedPreferences(HABIT_PREF, Context.MODE_MULTI_PROCESS);
        Gson gson = new Gson();
        // History keeps every simulated tick, state remembers what we checked last.
        Map<String, ArrayList<StockDataPoint>> stockHistory = loadStockHistory(habitPrefs, gson);
        Map<String, StockSimulationState> stockState = loadStockState(habitPrefs, gson);

        ArrayList<StockDataPoint> history = stockHistory.get(habitName);
        if (history == null) {
            history = new ArrayList<>();
            stockHistory.put(habitName, history);
        }

        StockSimulationState simulationState = stockState.get(habitName);
        if (simulationState == null) {
            simulationState = new StockSimulationState();
            stockState.put(habitName, simulationState);
        }

        long now = floorToHour(System.currentTimeMillis());
        double lastPrice;
        long lastTimestamp;

        if (history.isEmpty()) {
            // First run: seed the timeline using the saved starting value.
            lastPrice = Math.max(1d, selectedHabit.getBlocks3());
            lastTimestamp = now;
            history.add(new StockDataPoint(habitName, lastTimestamp, lastPrice));
        } else {
            StockDataPoint lastPoint = history.get(history.size() - 1);
            lastPrice = lastPoint.getPrice();
            lastTimestamp = lastPoint.getTimestamp();

            if (lastTimestamp > now) {
                saveStockHistory(habitPrefs, gson, stockHistory);
                saveStockState(habitPrefs, gson, stockState);
                return;
            }
        }

        long nextTimestamp = lastTimestamp + INTERVAL_MILLIS;
        while (nextTimestamp <= now) {
            if (isStartOfDay(nextTimestamp)) {
                Calendar previousDay = calendarFrom(nextTimestamp);
                previousDay.add(Calendar.DAY_OF_YEAR, -1);
                if (shouldEvaluateDay(previousDay, simulationState)) {
                    // Before the next day begins we settle yesterday's bonus or penalty.
                    DailyAdjustment adjustment = evaluateDailyPerformance(context, selectedHabit, habitName, previousDay);
                    lastPrice = applyDailyAdjustment(lastPrice, adjustment);
                    updateEvaluationState(simulationState, previousDay, adjustment.goalMet);
                }
            }

            // Apply the hourly wobble and store the new tick.
            double newPrice = applyHourlyFluctuation(lastPrice, simulationState.wasLastGoalMet());
            history.add(new StockDataPoint(habitName, nextTimestamp, newPrice));
            lastPrice = newPrice;
            lastTimestamp = nextTimestamp;
            nextTimestamp += INTERVAL_MILLIS;
        }

        saveStockHistory(habitPrefs, gson, stockHistory);
        saveStockState(habitPrefs, gson, stockState);
    }

    /**
     * Convenience helper used when the user logs minutes for a habit.
     */
    public static void onMinutesLogged(Context context, String habitName) {
        updateForHabit(context, habitName);
    }

    /**
     * Returns the stored stock history for the provided project.
     */
    public static ArrayList<StockDataPoint> getHistoryForHabit(Context context, String habitName) {
        if (context == null || habitName == null || habitName.isEmpty()) {
            return new ArrayList<>();
        }

        SharedPreferences habitPrefs = context.getSharedPreferences(HABIT_PREF, Context.MODE_MULTI_PROCESS);
        Gson gson = new Gson();
        Map<String, ArrayList<StockDataPoint>> stockHistory = loadStockHistory(habitPrefs, gson);
        ArrayList<StockDataPoint> history = stockHistory.get(habitName);
        if (history == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    private static DailyAdjustment evaluateDailyPerformance(Context context, DynamicHabit habit, String habitName, Calendar day) {
        int targetMinutes = habit.getTime();
        if (targetMinutes <= 0) {
            return new DailyAdjustment(0d, true);
        }

        // Count how many minutes the user logged on the previous day.
        double minutesWorked = getMinutesWorkedForDay(context, habitName, day.get(Calendar.DAY_OF_YEAR));
        if (minutesWorked >= targetMinutes) {
            double percentAbove = (minutesWorked - targetMinutes) / (double) targetMinutes;
            double adjustment = DAILY_GOAL_BONUS;
            if (percentAbove > 0d) {
                double exponentialBonus = Math.exp(percentAbove) - 1d;
                adjustment += exponentialBonus * DAILY_GOAL_BONUS;
            }
            return new DailyAdjustment(adjustment, true);
        }

        double percentMissing = (targetMinutes - minutesWorked) / (double) targetMinutes;
        double penalty = -Math.min(DAILY_MAX_PENALTY, DAILY_MAX_PENALTY * percentMissing);
        return new DailyAdjustment(penalty, false);
    }

    // Apply the daily bonus or penalty and make sure the value never drops below our minimum.
    private static double applyDailyAdjustment(double lastPrice, DailyAdjustment adjustment) {
        double adjusted = lastPrice * (1d + adjustment.delta);
        if (adjusted < MIN_PRICE) {
            adjusted = MIN_PRICE;
        }
        return adjusted;
    }

    private static double applyHourlyFluctuation(double lastPrice, boolean goalMet) {
        // Pick a random wiggle within the stable or unstable band.
        double minChange = goalMet ? STABLE_MIN_DELTA : UNSTABLE_MIN_DELTA;
        double maxChange = goalMet ? STABLE_MAX_DELTA : UNSTABLE_MAX_DELTA;
        double randomPercent = minChange + (maxChange - minChange) * RANDOM.nextDouble();
        double adjusted = lastPrice * (1d + randomPercent);
        if (adjusted < MIN_PRICE) {
            adjusted = MIN_PRICE;
        }
        return adjusted;
    }

    private static boolean shouldEvaluateDay(Calendar day, StockSimulationState state) {
        if (state == null) {
            return true;
        }
        int year = day.get(Calendar.YEAR);
        int dayOfYear = day.get(Calendar.DAY_OF_YEAR);

        if (state.getLastEvaluatedYear() < year) {
            return true;
        }
        if (state.getLastEvaluatedYear() > year) {
            return false;
        }
        return dayOfYear > state.getLastEvaluatedDayOfYear();
    }

    private static void updateEvaluationState(StockSimulationState state, Calendar day, boolean goalMet) {
        state.setLastEvaluatedYear(day.get(Calendar.YEAR));
        state.setLastEvaluatedDayOfYear(day.get(Calendar.DAY_OF_YEAR));
        state.setLastGoalMet(goalMet);
    }

    private static Calendar calendarFrom(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    private static boolean isStartOfDay(long timestamp) {
        Calendar calendar = calendarFrom(timestamp);
        return calendar.get(Calendar.HOUR_OF_DAY) == 0;
    }

    // Sum up all logged minutes for the habit on the requested day of year.
    private static double getMinutesWorkedForDay(Context context, String habitName, int dayOfYear) {
        if (context == null) {
            return 0d;
        }
        SharedPreferences userStorage = context.getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        String json = userStorage.getString("userStorageList", null);
        if (json == null) {
            return 0d;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        ArrayList<dataStorage> entries = gson.fromJson(json, type);
        if (entries == null || entries.isEmpty()) {
            return 0d;
        }

        double total = 0d;
        for (dataStorage entry : entries) {
            if (entry != null && habitName.equals(entry.getName()) && entry.getDate() == dayOfYear) {
                total += entry.getHours();
            }
        }
        return total;
    }

    private static long floorToHour(long timeMillis) {
        return timeMillis - (timeMillis % INTERVAL_MILLIS);
    }

    private static ArrayList<DynamicHabit> loadHabits(Context context) {
        // Habits live in PersonalHabits shared preferences as JSON.
        SharedPreferences habitPrefs = context.getSharedPreferences(HABIT_PREF, Context.MODE_MULTI_PROCESS);
        String json = habitPrefs.getString(HABIT_LIST_KEY, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private static Map<String, ArrayList<StockDataPoint>> loadStockHistory(SharedPreferences prefs, Gson gson) {
        // Each habit name maps to a list of hourly StockDataPoint items.
        String json = prefs.getString(STOCK_HISTORY_KEY, null);
        if (json == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<HashMap<String, ArrayList<StockDataPoint>>>(){}.getType();
        Map<String, ArrayList<StockDataPoint>> history = gson.fromJson(json, type);
        if (history == null) {
            history = new HashMap<>();
        }
        return history;
    }

    private static Map<String, StockSimulationState> loadStockState(SharedPreferences prefs, Gson gson) {
        // Simulation state keeps track of which day we last evaluated and if it hit the goal.
        String json = prefs.getString(STOCK_STATE_KEY, null);
        if (json == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<HashMap<String, StockSimulationState>>(){}.getType();
        Map<String, StockSimulationState> state = gson.fromJson(json, type);
        if (state == null) {
            state = new HashMap<>();
        }
        return state;
    }

    private static void saveStockHistory(SharedPreferences prefs, Gson gson, Map<String, ArrayList<StockDataPoint>> history) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STOCK_HISTORY_KEY, gson.toJson(history));
        editor.apply();
    }

    private static void saveStockState(SharedPreferences prefs, Gson gson, Map<String, StockSimulationState> state) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STOCK_STATE_KEY, gson.toJson(state));
        editor.apply();
    }

    private static class DailyAdjustment {
        final double delta;
        final boolean goalMet;

        DailyAdjustment(double delta, boolean goalMet) {
            this.delta = delta;
            this.goalMet = goalMet;
        }
    }
}

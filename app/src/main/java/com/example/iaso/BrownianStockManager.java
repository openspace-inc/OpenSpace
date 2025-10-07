package com.example.iaso;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.iaso.Model.StockDataPoint;
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
 * Generates Brownian motion-based stock prices for each personal project.
 */
public final class BrownianStockManager {

    private static final double K = 0.011; // +/- 1.1%
    private static final double C = 0.75; // fraction of k seen each day
    private static final double DT_HOURS = 1.0; // run every hour
    private static final long INTERVAL_MILLIS = (long) (DT_HOURS * 60 * 60 * 1000);
    private static final double SIGMA = (C * K) / (2 * Math.sqrt(24.0 / DT_HOURS));
    private static final double GOAL_IMPACT = 0.05; // 5% of progress delta applied to price

    private static final String HABIT_PREF = "PersonalHabits";
    private static final String HABIT_LIST_KEY = "personalHabitList";
    private static final String STOCK_HISTORY_KEY = "stockHistory";

    private static final Random RANDOM = new Random();

    private BrownianStockManager() {
        // Utility class
    }

    /**
     * Updates all saved projects with Brownian motion ticks up to the current hour.
     */
    public static void updateAll(Context context) {
        if (context == null) {
            return;
        }
        ArrayList<DynamicHabit> habits = loadHabits(context);
        if (habits == null || habits.isEmpty()) {
            return;
        }

        for (DynamicHabit habit : habits) {
            updateForHabit(context, habit.getName3());
        }
    }

    /**
     * Generates stock ticks for a single project up to the current hour.
     */
    public static void updateForHabit(Context context, String habitName) {
        if (context == null || habitName == null || habitName.isEmpty()) {
            return;
        }

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
        Map<String, ArrayList<StockDataPoint>> stockHistory = loadStockHistory(habitPrefs, gson);
        ArrayList<StockDataPoint> history = stockHistory.get(habitName);
        if (history == null) {
            history = new ArrayList<>();
            stockHistory.put(habitName, history);
        }

        long now = floorToHour(System.currentTimeMillis());
        double lastPrice;
        long lastTimestamp;

        if (history.isEmpty()) {
            lastPrice = Math.max(1d, selectedHabit.getBlocks3());
            lastTimestamp = now;
            history.add(new StockDataPoint(habitName, lastTimestamp, lastPrice));
        } else {
            StockDataPoint lastPoint = history.get(history.size() - 1);
            lastPrice = lastPoint.getPrice();
            lastTimestamp = lastPoint.getTimestamp();

            if (lastTimestamp > now) {
                // Future-dated data; do not generate new points
                saveStockHistory(habitPrefs, gson, stockHistory);
                return;
            }
        }

        long nextTimestamp = lastTimestamp + INTERVAL_MILLIS;
        while (nextTimestamp <= now) {
            double newPrice = generateNextPrice(context, selectedHabit, habitName, nextTimestamp, lastPrice);
            history.add(new StockDataPoint(habitName, nextTimestamp, newPrice));
            lastPrice = newPrice;
            nextTimestamp += INTERVAL_MILLIS;
        }

        saveStockHistory(habitPrefs, gson, stockHistory);
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

    private static double generateNextPrice(Context context, DynamicHabit habit, String habitName, long timestamp, double lastPrice) {
        double proposed = lastPrice * (1 + RANDOM.nextGaussian() * SIGMA);
        double adjustment = 1 + GOAL_IMPACT * computeProgressDelta(context, habit, habitName, timestamp);
        proposed *= adjustment;

        double lowerBound = lastPrice * (1 - K);
        double upperBound = lastPrice * (1 + K);

        double boundedPrice;
        if (proposed < lowerBound) {
            boundedPrice = (2 * lowerBound) - proposed;
        } else if (proposed > upperBound) {
            boundedPrice = (2 * upperBound) - proposed;
        } else {
            boundedPrice = proposed;
        }

        return Math.max(0.01d, boundedPrice);
    }

    private static double computeProgressDelta(Context context, DynamicHabit habit, String habitName, long timestamp) {
        int targetMinutes = habit.getTime();
        if (targetMinutes <= 0) {
            return 0d;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        double minutesWorked = getMinutesWorkedForDay(context, habitName, dayOfYear);
        double delta = (minutesWorked - targetMinutes) / (double) targetMinutes;
        // Clamp the delta to avoid extreme swings
        if (delta > 1.5d) {
            delta = 1.5d;
        } else if (delta < -1.5d) {
            delta = -1.5d;
        }
        return delta;
    }

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

    private static void saveStockHistory(SharedPreferences prefs, Gson gson, Map<String, ArrayList<StockDataPoint>> history) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(STOCK_HISTORY_KEY, gson.toJson(history));
        editor.apply();
    }
}

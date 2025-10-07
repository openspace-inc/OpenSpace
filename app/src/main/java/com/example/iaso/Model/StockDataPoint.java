package com.example.iaso.Model;

public class StockDataPoint {
    private String habitName;
    private long timestamp;
    private double price;

    public StockDataPoint() {
        // Required empty constructor for serialization frameworks
    }

    public StockDataPoint(String habitName, long timestamp, double price) {
        this.habitName = habitName;
        this.timestamp = timestamp;
        this.price = price;
    }

    public String getHabitName() {
        return habitName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }
}

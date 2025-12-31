package com.example.iaso.Home;

public class HabitTimeData {
    private String habitName;
    private String imageName;
    private int totalMinutes;

    public HabitTimeData(String habitName, String imageName, int totalMinutes) {
        this.habitName = habitName;
        this.imageName = imageName;
        this.totalMinutes = totalMinutes;
    }

    public String getHabitName() {
        return habitName;
    }

    public String getImageName() {
        return imageName;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }
}

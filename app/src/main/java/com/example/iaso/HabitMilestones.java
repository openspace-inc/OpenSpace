package com.example.iaso;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class that stores a habit's name and its associated milestones.
 * This is used to store and retrieve milestone data from SharedPreferences.
 */
public class HabitMilestones {
    /** The name/ticker symbol of the habit/project */
    private String habitName;

    /** List of milestones for this habit */
    private List<Milestone> milestones;

    /**
     * Constructor
     * @param habitName The name/ticker symbol of the habit
     * @param milestones List of milestones for this habit
     */
    public HabitMilestones(String habitName, List<Milestone> milestones) {
        this.habitName = habitName;
        this.milestones = milestones != null ? milestones : new ArrayList<>();
    }

    /**
     * Default constructor for Gson deserialization
     */
    public HabitMilestones() {
        this.habitName = "";
        this.milestones = new ArrayList<>();
    }

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones != null ? milestones : new ArrayList<>();
    }

    /**
     * Gets the total number of days across all milestones
     * @return Total days for completing all milestones
     */
    public int getTotalDays() {
        int total = 0;
        for (Milestone milestone : milestones) {
            total += milestone.getDays();
        }
        return total;
    }

    /**
     * Gets the number of milestones in this habit
     * @return Count of milestones
     */
    public int getMilestoneCount() {
        return milestones.size();
    }
}

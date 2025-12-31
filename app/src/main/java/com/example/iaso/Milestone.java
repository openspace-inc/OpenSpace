package com.example.iaso;

/**
 * Model class representing a single milestone in the user's goal path.
 * Contains the milestone name, the time duration string, and parsed days value.
 */
public class Milestone {
    /** The name/description of the milestone */
    private String name;

    /** The time duration as a string (e.g., "30 days", "2 weeks") */
    private String time;

    /** The parsed number of days for this milestone */
    private int days;

    /**
     * Constructor with name and time string
     * @param name The milestone name/description
     * @param time The time duration string
     */
    public Milestone(String name, String time) {
        this.name = name;
        this.time = time;
        this.days = extractDaysFromTime(time);
    }

    /**
     * Constructor with all fields (used when deserializing from JSON)
     * @param name The milestone name/description
     * @param time The time duration string
     * @param days The number of days for this milestone
     */
    public Milestone(String name, String time, int days) {
        this.name = name;
        this.time = time;
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        // Re-calculate days when time is updated
        this.days = extractDaysFromTime(time);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    /**
     * Extracts the number of days from a time string like "55 days" or "30 days"
     * @param timeString The time string to parse
     * @return Number of days, or 0 if parsing fails
     */
    private int extractDaysFromTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        // Extract numbers from the string
        String numbersOnly = timeString.replaceAll("[^0-9]", "");
        if (numbersOnly.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(numbersOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

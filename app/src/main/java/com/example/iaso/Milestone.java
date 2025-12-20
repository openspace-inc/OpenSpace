package com.example.iaso;

/**
 * Model class representing a single milestone in the user's goal path.
 * Contains the milestone name and the time duration.
 */
public class Milestone {
    private String name;
    private String time;

    public Milestone(String name, String time) {
        this.name = name;
        this.time = time;
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
    }
}

package com.example.iaso.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//MatrixGoal.java — Represents one habit/goal a user is working toward (e.g., "Run every day"). It stores:
//  - A name, description, and duration in days
//  - A start date and auto-calculated target/end date
//  - A status: ACTIVE, PAUSED, or COMPLETED
//  - A list of milestones attached to it

public class MatrixGoal {

    public enum Status {
        ACTIVE,
        PAUSED,
        COMPLETED
    }

    private String goalId;
    private String habitName;
    private String goalDescription;
    private int totalDays;
    private int bufferDays;
    private long startDate;
    private long targetDate;
    private Status status;
    private long createdAt;
    private List<MatrixMilestone> milestones;

    public MatrixGoal(String habitName, String goalDescription, int totalDays, long startDate) {
        if (totalDays < 0) throw new IllegalArgumentException("totalDays must be non-negative");
        if (startDate < 0) throw new IllegalArgumentException("startDate must be non-negative");

        this.goalId          = UUID.randomUUID().toString();
        this.habitName       = habitName;
        this.goalDescription = goalDescription;
        this.totalDays       = totalDays;
        this.bufferDays      = 0;
        this.startDate       = startDate;
        this.targetDate      = computeTargetDate(startDate, totalDays);
        this.status          = Status.ACTIVE;
        this.createdAt       = System.currentTimeMillis();
        this.milestones      = new ArrayList<>();
    }

    public MatrixGoal() {
        this.goalId     = UUID.randomUUID().toString();
        this.status     = Status.ACTIVE;
        this.createdAt  = System.currentTimeMillis();
        this.bufferDays = 0;
        this.milestones = new ArrayList<>();
    }

    private static long computeTargetDate(long startDate, int totalDays) {
        return startDate + TimeUnit.DAYS.toMillis(totalDays);
    }

    public String getGoalId()                    { return goalId; }
    public void   setGoalId(String goalId)       { this.goalId = goalId; }

    public String getHabitName()                 { return habitName; }
    public void   setHabitName(String habitName) { this.habitName = habitName; }

    public String getGoalDescription()                       { return goalDescription; }
    public void   setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) {
        if (totalDays < 0) throw new IllegalArgumentException("totalDays must be non-negative");
        this.totalDays  = totalDays;
        this.targetDate = computeTargetDate(this.startDate, totalDays);
    }

    public int  getBufferDays() { return bufferDays; }
    public void setBufferDays(int bufferDays) {
        if (bufferDays < 0) throw new IllegalArgumentException("bufferDays must be non-negative");
        this.bufferDays = bufferDays;
    }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) {
        if (startDate < 0) throw new IllegalArgumentException("startDate must be non-negative");
        this.startDate  = startDate;
        this.targetDate = computeTargetDate(startDate, this.totalDays);
    }

    public long getTargetDate()                { return targetDate; }
    /** Restores a persisted targetDate during deserialization. Use setTotalDays/setStartDate for computed updates. */
    public void setTargetDate(long targetDate) { this.targetDate = targetDate; }

    public Status getStatus()              { return status; }
    public void   setStatus(Status status) { this.status = status; }

    public long getCreatedAt()               { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<MatrixMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<MatrixMilestone> milestones) {
        this.milestones = milestones != null ? milestones : new ArrayList<>();
    }

    public boolean isActive() { return status == Status.ACTIVE; }

    public void complete() { this.status = Status.COMPLETED; }
}

package com.example.iaso.matrix;

import java.util.UUID;

public class matrixgoal {

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

    public matrixgoal(String habitName, String goalDescription, int totalDays, long startDate) {
        this.goalId = UUID.randomUUID().toString();
        this.habitName = habitName;
        this.goalDescription = goalDescription;
        this.totalDays = totalDays;
        this.bufferDays = 0;
        this.startDate = startDate;
        this.targetDate = startDate + ((long) totalDays * 24 * 60 * 60 * 1000L);
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public matrixgoal() {
        this.goalId = UUID.randomUUID().toString();
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.bufferDays = 0;
    }

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getHabitName() { return habitName; }
    public void setHabitName(String habitName) { this.habitName = habitName; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getBufferDays() { return bufferDays; }
    public void setBufferDays(int bufferDays) { this.bufferDays = bufferDays; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getTargetDate() { return targetDate; }
    public void setTargetDate(long targetDate) { this.targetDate = targetDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public void complete() {
        this.status = Status.COMPLETED;
    }
}
package com.example.iaso.matrix;

public class matrixsnapshot {

    private String activeGoalId;

    private int activeMilestoneIndex;
    private int currentDayInMilestone;
    private int totalDaysElapsed;
    private int totalDaysRemaining;
    private int bufferRemaining;
    private long lastUpdated;

    public matrixsnapshot(String activeGoalId, int activeMilestoneIndex,
                            int currentDayInMilestone, int totalDaysElapsed,
                            int totalDaysRemaining, int bufferRemaining) {
        this.activeGoalId = activeGoalId;
        this.activeMilestoneIndex = activeMilestoneIndex;
        this.currentDayInMilestone = currentDayInMilestone;
        this.totalDaysElapsed = totalDaysElapsed;
        this.totalDaysRemaining = totalDaysRemaining;
        this.bufferRemaining = bufferRemaining;
        this.lastUpdated = System.currentTimeMillis();
    }

    public matrixsnapshot() {
        this.bufferRemaining = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getActiveGoalId() { return activeGoalId; }
    public void setActiveGoalId(String activeGoalId) { this.activeGoalId = activeGoalId; }

    public int getActiveMilestoneIndex() { return activeMilestoneIndex; }
    public void setActiveMilestoneIndex(int activeMilestoneIndex) { this.activeMilestoneIndex = activeMilestoneIndex; }

    public int getCurrentDayInMilestone() { return currentDayInMilestone; }
    public void setCurrentDayInMilestone(int currentDayInMilestone) { this.currentDayInMilestone = currentDayInMilestone; }

    public int getTotalDaysElapsed() { return totalDaysElapsed; }
    public void setTotalDaysElapsed(int totalDaysElapsed) { this.totalDaysElapsed = totalDaysElapsed; }

    public int getTotalDaysRemaining() { return totalDaysRemaining; }
    public void setTotalDaysRemaining(int totalDaysRemaining) { this.totalDaysRemaining = totalDaysRemaining; }

    public int getBufferRemaining() { return bufferRemaining; }
    public void setBufferRemaining(int bufferRemaining) { this.bufferRemaining = bufferRemaining; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public void touch() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public float getProgressPercent(int totalGoalDays) {
        if (totalGoalDays <= 0) return 0f;
        return Math.min(100f, (totalDaysElapsed * 100f) / totalGoalDays);
    }

    @Override
    public String toString() {
        return "matrixsnapshot{" +
                "goal=" + activeGoalId +
                ", milestone=" + activeMilestoneIndex +
                ", dayInMilestone=" + currentDayInMilestone +
                ", elapsed=" + totalDaysElapsed +
                ", remaining=" + totalDaysRemaining +
                ", buffer=" + bufferRemaining +
                "}";
    }
}
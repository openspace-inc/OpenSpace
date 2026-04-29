package com.example.iaso.matrix;

public class MatrixSnapshot {

    private String activeGoalId;
    private int activeMilestoneIndex;
    private int currentDayInMilestone;
    private int totalDaysElapsed;
    private int totalDaysRemaining;
    private int bufferRemaining;
    private long lastUpdated;

    public MatrixSnapshot(String activeGoalId,
                          int activeMilestoneIndex,
                          int currentDayInMilestone,
                          int totalDaysElapsed,
                          int totalDaysRemaining,
                          int bufferRemaining) {
        this(activeGoalId,
                activeMilestoneIndex,
                currentDayInMilestone,
                totalDaysElapsed,
                totalDaysRemaining,
                bufferRemaining,
                System.currentTimeMillis());
    }

    public MatrixSnapshot(String activeGoalId,
                          int activeMilestoneIndex,
                          int currentDayInMilestone,
                          int totalDaysElapsed,
                          int totalDaysRemaining,
                          int bufferRemaining,
                          long lastUpdated) {
        if (activeMilestoneIndex < 0)  throw new IllegalArgumentException("activeMilestoneIndex must be non-negative");
        if (currentDayInMilestone < 1) throw new IllegalArgumentException("currentDayInMilestone must be >= 1");
        if (totalDaysElapsed < 0)      throw new IllegalArgumentException("totalDaysElapsed must be non-negative");
        if (totalDaysRemaining < 0)    throw new IllegalArgumentException("totalDaysRemaining must be non-negative");
        if (bufferRemaining < 0)       throw new IllegalArgumentException("bufferRemaining must be non-negative");

        this.activeGoalId          = activeGoalId;
        this.activeMilestoneIndex  = activeMilestoneIndex;
        this.currentDayInMilestone = currentDayInMilestone;
        this.totalDaysElapsed      = totalDaysElapsed;
        this.totalDaysRemaining    = totalDaysRemaining;
        this.bufferRemaining       = bufferRemaining;
        this.lastUpdated           = lastUpdated;
    }

    public MatrixSnapshot() {
        this.currentDayInMilestone = 1;
        this.bufferRemaining       = 0;
        this.lastUpdated           = System.currentTimeMillis();
    }

    public String getActiveGoalId()                    { return activeGoalId; }
    public void   setActiveGoalId(String activeGoalId) { this.activeGoalId = activeGoalId; }

    public int  getActiveMilestoneIndex() { return activeMilestoneIndex; }
    public void setActiveMilestoneIndex(int activeMilestoneIndex) {
        if (activeMilestoneIndex < 0) throw new IllegalArgumentException("activeMilestoneIndex must be non-negative");
        this.activeMilestoneIndex = activeMilestoneIndex;
    }

    public int  getCurrentDayInMilestone() { return currentDayInMilestone; }
    public void setCurrentDayInMilestone(int currentDayInMilestone) {
        if (currentDayInMilestone < 1) throw new IllegalArgumentException("currentDayInMilestone must be >= 1");
        this.currentDayInMilestone = currentDayInMilestone;
    }

    public int  getTotalDaysElapsed() { return totalDaysElapsed; }
    public void setTotalDaysElapsed(int totalDaysElapsed) {
        if (totalDaysElapsed < 0) throw new IllegalArgumentException("totalDaysElapsed must be non-negative");
        this.totalDaysElapsed = totalDaysElapsed;
    }

    public int  getTotalDaysRemaining() { return totalDaysRemaining; }
    public void setTotalDaysRemaining(int totalDaysRemaining) {
        if (totalDaysRemaining < 0) throw new IllegalArgumentException("totalDaysRemaining must be non-negative");
        this.totalDaysRemaining = totalDaysRemaining;
    }

    public int  getBufferRemaining() { return bufferRemaining; }
    public void setBufferRemaining(int bufferRemaining) {
        if (bufferRemaining < 0) throw new IllegalArgumentException("bufferRemaining must be non-negative");
        this.bufferRemaining = bufferRemaining;
    }

    public long getLastUpdated()                 { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public void touch() { this.lastUpdated = System.currentTimeMillis(); }

    public float getProgressPercent(int totalGoalDays) {
        if (totalGoalDays <= 0) return 0f;
        return Math.min(100f, (totalDaysElapsed * 100f) / totalGoalDays);
    }

    @Override
    public String toString() {
        return "MatrixSnapshot{" +
                "goal=" + activeGoalId +
                ", milestone=" + activeMilestoneIndex +
                ", dayInMilestone=" + currentDayInMilestone +
                ", elapsed=" + totalDaysElapsed +
                ", remaining=" + totalDaysRemaining +
                ", buffer=" + bufferRemaining +
                "}";
    }
}

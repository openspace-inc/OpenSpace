package com.example.iaso.Model;

// Tracks the last day we evaluated a habit and if that day met the goal.
public class StockSimulationState {
    private int lastEvaluatedYear;
    private int lastEvaluatedDayOfYear;
    private boolean lastGoalMet;

    public StockSimulationState() {
        lastEvaluatedYear = -1;
        lastEvaluatedDayOfYear = -1;
        lastGoalMet = true;
    }

    public int getLastEvaluatedYear() {
        return lastEvaluatedYear;
    }

    public void setLastEvaluatedYear(int lastEvaluatedYear) {
        this.lastEvaluatedYear = lastEvaluatedYear;
    }

    public int getLastEvaluatedDayOfYear() {
        return lastEvaluatedDayOfYear;
    }

    public void setLastEvaluatedDayOfYear(int lastEvaluatedDayOfYear) {
        this.lastEvaluatedDayOfYear = lastEvaluatedDayOfYear;
    }

    public boolean wasLastGoalMet() {
        return lastGoalMet;
    }

    public void setLastGoalMet(boolean lastGoalMet) {
        this.lastGoalMet = lastGoalMet;
    }
}

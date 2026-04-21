package com.example.iaso.matrix;

import java.util.UUID;

public class matrixdailytaskslot{

    public enum Status {
        PENDING,
        COMPLETED,
        MISSED,
        RESCHEDULED
    }

    private String slotId;
    private String parentMilestoneId;
    private int dayNumber;
    private long date;
    private Status status;
    private String taskPayload;

    public matrixdailytaskslot(String parentMilestoneId, int dayNumber, long date) {
        if (dayNumber < 1) throw new IllegalArgumentException("dayNumber must be >= 1");
        if (date < 0)      throw new IllegalArgumentException("date must be non-negative");

        this.slotId            = UUID.randomUUID().toString();
        this.parentMilestoneId = parentMilestoneId;
        this.dayNumber         = dayNumber;
        this.date              = date;
        this.status            = Status.PENDING;
        this.taskPayload       = "";
    }

    public matrixdailytaskslot() {
        this.slotId      = UUID.randomUUID().toString();
        this.dayNumber   = 1;
        this.status      = Status.PENDING;
        this.taskPayload = "";
    }

    public String getSlotId()              { return slotId; }
    public void   setSlotId(String slotId) { this.slotId = slotId; }

    public String getParentMilestoneId()                         { return parentMilestoneId; }
    public void   setParentMilestoneId(String parentMilestoneId) { this.parentMilestoneId = parentMilestoneId; }

    public int  getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) {
        if (dayNumber < 1) throw new IllegalArgumentException("dayNumber must be >= 1");
        this.dayNumber = dayNumber;
    }

    public long getDate()          { return date; }
    public void setDate(long date) {
        if (date < 0) throw new IllegalArgumentException("date must be non-negative");
        this.date = date;
    }

    public Status getStatus()              { return status; }
    public void   setStatus(Status status) { this.status = status; }

    public String getTaskPayload()                   { return taskPayload; }
    public void   setTaskPayload(String taskPayload) { this.taskPayload = taskPayload != null ? taskPayload : ""; }

    public boolean hasTask()      { return taskPayload != null && !taskPayload.isEmpty(); }

    public boolean isCompleted()  { return status == Status.COMPLETED; }

    public void markCompleted()   { this.status = Status.COMPLETED; }

    public void markMissed()      { this.status = Status.MISSED; }

    public void markRescheduled() { this.status = Status.RESCHEDULED; }
}
package com.example.iaso.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class matrixmilestone {

    public enum Status {
        PENDING,
        ACTIVE,
        COMPLETED,
        SKIPPED
    }

    private String milestoneId;
    private String parentGoalId;
    private String name;
    private String description;
    private int orderIndex;
    private int allocatedDays;
    private int bufferDays;
    private int startDay;
    private Status status;
    private List<matrixdailytaskslot> dailyTaskSlots;

    public matrixmilestone(String parentGoalId, String name, String description,
                            int orderIndex, int allocatedDays, int startDay) {
        this(UUID.randomUUID().toString(), parentGoalId, name, description, orderIndex, allocatedDays, 0, startDay);
    }

    public matrixmilestone(String milestoneId, String parentGoalId, String name, String description,
                            int orderIndex, int allocatedDays, int bufferDays, int startDay) {
        if (allocatedDays < 0) throw new IllegalArgumentException("allocatedDays must be non-negative");
        if (orderIndex < 0)    throw new IllegalArgumentException("orderIndex must be non-negative");
        if (bufferDays < 0)    throw new IllegalArgumentException("bufferDays must be non-negative");
        if (startDay < 0)      throw new IllegalArgumentException("startDay must be non-negative");

        this.milestoneId    = milestoneId;
        this.parentGoalId   = parentGoalId;
        this.name           = name;
        this.description    = description;
        this.orderIndex     = orderIndex;
        this.allocatedDays  = allocatedDays;
        this.bufferDays     = bufferDays;
        this.startDay       = startDay;
        this.status         = Status.PENDING;
        this.dailyTaskSlots = new ArrayList<>();
    }

    public matrixmilestone() {
        this.milestoneId    = UUID.randomUUID().toString();
        this.status         = Status.PENDING;
        this.bufferDays     = 0;
        this.dailyTaskSlots = new ArrayList<>();
    }

    public String getMilestoneId()                     { return milestoneId; }
    public void   setMilestoneId(String milestoneId)   { this.milestoneId = milestoneId; }

    public String getParentGoalId()                    { return parentGoalId; }
    public void   setParentGoalId(String parentGoalId) { this.parentGoalId = parentGoalId; }

    public String getName()            { return name; }
    public void   setName(String name) { this.name = name; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public int  getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) {
        if (orderIndex < 0) throw new IllegalArgumentException("orderIndex must be non-negative");
        this.orderIndex = orderIndex;
    }

    public int  getAllocatedDays() { return allocatedDays; }
    public void setAllocatedDays(int allocatedDays) {
        if (allocatedDays < 0) throw new IllegalArgumentException("allocatedDays must be non-negative");
        this.allocatedDays = allocatedDays;
    }

    public int  getBufferDays() { return bufferDays; }
    public void setBufferDays(int bufferDays) {
        if (bufferDays < 0) throw new IllegalArgumentException("bufferDays must be non-negative");
        this.bufferDays = bufferDays;
    }

    public int  getStartDay() { return startDay; }
    public void setStartDay(int startDay) {
        if (startDay < 0) throw new IllegalArgumentException("startDay must be non-negative");
        this.startDay = startDay;
    }

    public Status getStatus()              { return status; }
    public void   setStatus(Status status) { this.status = status; }

    public List<matrixdailytaskslot> getDailyTaskSlots() { return dailyTaskSlots; }
    public void setDailyTaskSlots(List<matrixdailytaskslot> slots) {
        this.dailyTaskSlots = slots != null ? slots : new ArrayList<>();
    }

    public int getEndDay() {
        int total = allocatedDays + bufferDays;
        if (total <= 0) return startDay;
        return startDay + total - 1;
    }

    public boolean isActive() { return status == Status.ACTIVE; }

    public void activate() { this.status = Status.ACTIVE; }

    public void complete() { this.status = Status.COMPLETED; }
}
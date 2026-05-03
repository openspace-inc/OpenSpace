package com.example.iaso;

/**
 * Model class representing a single to-do task associated with a habit.
 * Tasks are stored per-habit in SharedPreferences and displayed in
 * the habit detail expansion panel.
 */
public class HabitTask {
    private String taskText;
    private boolean completed;
    private long createdAt;

    /**
     * Creates a new incomplete task with the current timestamp.
     * @param taskText The text description of the task
     */
    public HabitTask(String taskText) {
        this.taskText = taskText;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

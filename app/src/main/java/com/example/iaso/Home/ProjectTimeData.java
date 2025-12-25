package com.example.iaso.Home;

public class ProjectTimeData {
    private String projectName;
    private String imageName;
    private double totalMinutes;

    public ProjectTimeData(String projectName, String imageName, double totalMinutes) {
        this.projectName = projectName;
        this.imageName = imageName;
        this.totalMinutes = totalMinutes;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getImageName() {
        return imageName;
    }

    public double getTotalMinutes() {
        return totalMinutes;
    }

    public void addMinutes(double minutes) {
        this.totalMinutes += minutes;
    }
}

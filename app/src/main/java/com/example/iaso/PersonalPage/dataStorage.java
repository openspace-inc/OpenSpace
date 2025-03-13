package com.example.iaso.PersonalPage;

public class dataStorage {
    String name; //name of the object/goal we are saving for
    String type; //used for organization purposes and to seperate between personal projects and health
    double hours; //store the hours worked on a project

    public dataStorage(String name, String type, double hours){
        this.name = name;
        this.type = type;
        this.hours = hours;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public double getHours(){
        return hours;
    }
}

package com.example.iaso.PersonalPage;

import java.util.Calendar;

public class dataStorage {
    String name; //name of the object/goal we are saving for
    String type; //used for organization purposes and to seperate between personal projects and health
    double hours; //store the hours worked on a project
    int day;

    public dataStorage(String name, String type, double hours){
        this.name = name;
        this.type = type;
        this.hours = hours;
        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_YEAR);
    }

    public dataStorage(String name, String type, double hours, int date){
        this.name = name;
        this.type = type;
        this.hours = hours;
        this.day = date;
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

    public int getDate(){
        return day;
    }
}

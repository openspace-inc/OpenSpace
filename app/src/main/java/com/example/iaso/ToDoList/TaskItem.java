package com.example.iaso.ToDoList;

public class TaskItem {

    String name;
    String date;

    public TaskItem (String name1, String date1){
        name = name1;
        date = date1;
    }

    public String getName(){
        return name;
    }
    public String getDate(){
        return date;
    }
}

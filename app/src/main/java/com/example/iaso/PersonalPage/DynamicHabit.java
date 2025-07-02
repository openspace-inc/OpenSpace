package com.example.iaso.PersonalPage;

public class DynamicHabit {
    String name;
    int blocks;
    int streak;
    String type;
    String description;
    int amount;
    int time;
    String imageName;
    int timeInvested;

    public DynamicHabit(String name1, int streak1, String type1, String description1, int amount1, int time1, String imageName1, int timeInvested1){
        name = name1;
        streak = streak1;
        type = type1;
        description = description1;
        blocks = 1;
        amount = amount1;
        time = time1;
        imageName = imageName1;
        timeInvested = timeInvested1;
    }

    public String getName3(){
        return name;
    }

    public String getType(){
        return type;
    }

    public int getStreak3(){
        return streak;
    }

    public int getBlocks3(){
        return blocks;
    }
    public String getDescription(){
        return description;
    }
    public int getAmount3(){return amount;}

    public int getTime(){
        return time;
    }
    public String getImageName(){
        return imageName;
    }
    public int getTimeInvested(){return timeInvested;}
}

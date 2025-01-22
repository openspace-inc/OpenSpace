package com.example.iaso.Health;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

public class healthyHabits {
    private String name;
    private String difficulty;
    private int pointValue;
    int habitImage;
    String habitDescription;

    public healthyHabits(String name1, String difficulty1, int pointValue1, int image, String description){
        name = name1;
        difficulty = difficulty1;
        pointValue = pointValue1;
        habitImage = image;
        habitDescription = description;
    }

    public String getName(){
        return name;
    }

    public String getDifficulty(){
        return difficulty;
    }

    public int getPointValue(){
        return pointValue;
    }

    public int getHabitImage(){
        return habitImage;
    }
    public String getDescription(){
        return habitDescription;
    }
}

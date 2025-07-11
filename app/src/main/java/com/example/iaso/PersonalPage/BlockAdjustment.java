package com.example.iaso.PersonalPage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BlockAdjustment {

    public static void adjustBlocks(Context context, String projectName){
        if(projectName == null || context == null) return;

        //Open user storage to pull all logged data
        SharedPreferences userStorage = context.getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        String json = userStorage.getString("userStorageList", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        ArrayList<dataStorage> storage = gson.fromJson(json, type);
        if(storage == null) return;

        //Filter for this project name
        ArrayList<dataStorage> projectData = new ArrayList<>();
        for(dataStorage d : storage){
            if(projectName.equals(d.getName())){
                projectData.add(d);
            }
        }

        if(projectData.size() <= 5) return; //not enough data

        //Sort by date to ensure correct order
        Collections.sort(projectData, new Comparator<dataStorage>(){
            @Override
            public int compare(dataStorage o1, dataStorage o2){
                return Integer.compare(o1.getDate(), o2.getDate());
            }
        });

        int size = projectData.size();
        double avg = (projectData.get(size-1).getHours() +
                projectData.get(size-2).getHours() +
                projectData.get(size-3).getHours()) / 3.0;

        //Open dynamic habits
        SharedPreferences dynamicHabits = context.getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS);
        String jsonHabits = dynamicHabits.getString("personalHabitList", null);
        Type habitType = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        ArrayList<DynamicHabit> dynamicHabitList = gson.fromJson(jsonHabits, habitType);
        if(dynamicHabitList == null) return;

        for(int i=0; i<dynamicHabitList.size(); i++){
            DynamicHabit habit = dynamicHabitList.get(i);
            if(projectName.equals(habit.getName3())){
                int currentTime = habit.getTime();
                int currentBlocks = habit.getBlocks3();

                double percentChange = (avg - currentTime) / (double) currentTime;
                double adjustment;
                if(percentChange == 0){
                    adjustment = 0.05;
                } else if(percentChange > 0){
                    adjustment = percentChange + 0.05;
                } else {
                    adjustment = percentChange;
                }
                int newBlocks = (int) Math.round(currentBlocks * (1 + adjustment));
                if(newBlocks < 1) newBlocks = 1;

                habit.blocks = newBlocks;
                break;
            }
        }

        //Store updated habits
        String updated = gson.toJson(dynamicHabitList);
        SharedPreferences.Editor editor = dynamicHabits.edit();
        editor.putString("personalHabitList", updated);
        editor.apply();
    }
}

package com.example.iaso;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iaso.PersonalPage.DynamicHabit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load user data from SharedPreferences
        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String name = userData.getString("name", "");
        int userPoints = userData.getInt("userPoints", 0);
        String age = userData.getString("age", "--");
        String gender = userData.getString("gender", "--");
        String weight = userData.getString("weight", "--");
        String height = userData.getString("height", "--");
        String exercise = userData.getString("exercise", "--");

        // Load projects from PersonalHabits SharedPreferences
        SharedPreferences personalHabitsPrefs = getSharedPreferences("PersonalHabits", Context.MODE_PRIVATE);
        String personalHabitsJson = personalHabitsPrefs.getString("personalHabitList", "[]");

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        List<DynamicHabit> habitList = gson.fromJson(personalHabitsJson, listType);

        // Calculate statistics
        int totalInvestmentTime = 0;
        int projectsCreated = habitList != null ? habitList.size() : 0;

        if (habitList != null) {
            for (DynamicHabit habit : habitList) {
                totalInvestmentTime += habit.getTimeInvested();
            }
        }

        // Update UI with data
        TextView profileName = findViewById(R.id.profileName);
        profileName.setText(name);

        TextView statTotalPoints = findViewById(R.id.statTotalPoints);
        statTotalPoints.setText(String.valueOf(userPoints));

        TextView statTotalTime = findViewById(R.id.statTotalTime);
        if (totalInvestmentTime >= 60) {
            double hours = totalInvestmentTime / 60.0;
            statTotalTime.setText(String.format("%.1f Hours", hours));
        } else {
            statTotalTime.setText(totalInvestmentTime + " Min");
        }

        TextView statProjectsCreated = findViewById(R.id.statProjectsCreated);
        statProjectsCreated.setText(String.valueOf(projectsCreated));

        TextView statAge = findViewById(R.id.statAge);
        statAge.setText(age);

        TextView statGender = findViewById(R.id.statGender);
        statGender.setText(gender);

        TextView statWeight = findViewById(R.id.statWeight);
        statWeight.setText(weight);

        TextView statHeight = findViewById(R.id.statHeight);
        statHeight.setText(height);

        TextView statExercise = findViewById(R.id.statExercise);
        statExercise.setText(exercise);
    }
}
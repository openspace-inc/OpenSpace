package com.example.iaso.Introduction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.iaso.Home.MainActivity;
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.dataStorage;
import com.example.iaso.R;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class WelcomeActivity3 extends AppCompatActivity {

    SharedPreferences userData; //general information about the user
    SharedPreferences personalHabits;
    SharedPreferences userStorage;
    CardView a;

    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    public ArrayList<dataStorage> storage = new ArrayList<>(); //Storage for habits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);
        //Fullscreen code
        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        //Create sharedPreference
        userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        TextView description = findViewById(R.id.data);
        description.setText("Let's customize your experience. \n Don't worry! All of this data will remain local " +
                "and secure. It never comes to us, anybody else, and as a matter of fact, never even leaves your device. \n" +
                "That's our promise to you.");

        CardView x = findViewById(R.id.cardView2);
        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        x.startAnimation(fadeInAnimation2);

        a = findViewById(R.id.oneFinalNote);
        a.setVisibility(View.GONE);


        //Clear EditText Name For User Upon Press
        EditText nameBox, ageBox, genderBox,weightBox, heightBox, exerciseBox;
        nameBox = findViewById(R.id.name);
        ageBox = findViewById(R.id.age);
        genderBox = findViewById(R.id.gender);
        weightBox = findViewById(R.id.weight);
        heightBox = findViewById(R.id.height);
        exerciseBox = findViewById(R.id.exercise);


        //Clear EditText Name For User Upon Press
        nameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    nameBox.setText("");  // Clear the text
                }
            }
        });
        nameBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameBox.setText("");
            }
        });
        ageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ageBox.setText("");  // Clear the text
                }
            }
        });
        ageBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageBox.setText("");
            }
        });
        genderBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    genderBox.setText("");  // Clear the text
                }
            }
        });
        genderBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                genderBox.setText("");
            }
        });
        weightBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    weightBox.setText("");  // Clear the text
                }
            }
        });
        weightBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weightBox.setText("");
            }
        });
        heightBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    heightBox.setText("");  // Clear the text
                }
            }
        });
        heightBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                heightBox.setText("");
            }
        });
        exerciseBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    exerciseBox.setText("");  // Clear the text
                }
            }
        });
        exerciseBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exerciseBox.setText("");
            }
        });
        //endregion
    }


    public void oneFinalNote(View v){
        //Final notice to users before beginning of the app.
        a.setVisibility(View.VISIBLE);
        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        a.startAnimation(fadeInAnimation2);

        TextView note = findViewById(R.id.description12);
        note.setText("Welcome to Iaso! \n You are currently a BETA member." +
                " Please be patient as we get things up and running. " +
                "As a thank you all BETA members will recieve our premium model for free when it comes" +
                " out early 2025. \n Thanks for being with us.");
    }

    public void mainActivity(View v){
        //Adding all data collected from user to SharedPref called UserData.
        Intent b = new Intent(this, MainActivity.class);
        String name = ((EditText)findViewById(R.id.name)).getText().toString();
        String age = ((EditText)findViewById(R.id.age)).getText().toString();
        String gender = ((EditText)findViewById(R.id.gender)).getText().toString();
        String weight = ((EditText)findViewById(R.id.weight)).getText().toString();
        String height = ((EditText)findViewById(R.id.height)).getText().toString();
        String exercise = ((EditText)findViewById(R.id.exercise)).getText().toString();

        SharedPreferences.Editor editor = userData.edit();
        editor.putString("name", name);
        editor.putString("age", age);
        editor.putString("gender", gender);
        editor.putString("weight", weight);
        editor.putString("height", height);
        editor.putString("exercise", exercise);
        editor.putString("previousDate", "00000000");
        editor.putString("bmr","");
        editor.putInt("index1", -1);
        editor.putInt("index2", -1);
        editor.putInt("userPoints", 0);
        editor.putBoolean("firstRun", true);
        editor.putBoolean("firstRunTaskLister", true);
        editor.apply();

        //Call to create dynamicHabit List for personal page.
        dynamicHabits();
        //initialize storage for users
        initalizeUserStorage();

        startActivity(b);

    }

    //implement userStorage for all habits.
    public void initalizeUserStorage(){
        //Create userStorage arraylst for the app
        userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor dataStorage = userStorage.edit();


        String json = userStorage.getString("userStorageList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        storage = gson.fromJson(json,type);


        if (storage == null) {
            storage = new ArrayList<dataStorage>();
        }
        else {
            String x = "just a placer";
        }

        //Turning ArrayList into JSON and then applying.
        json = gson.toJson(storage);
        dataStorage.putString("userStorageList", json);
        dataStorage.apply();
    }

    //implement dynamicHabits for the user
    public void dynamicHabits(){
        //Create dynamicHabit List for personal page.
        personalHabits = getSharedPreferences("PersonalHabits", Context.MODE_PRIVATE);
        SharedPreferences.Editor dynamicHabitEditor = personalHabits.edit();


        String json = personalHabits.getString("personalHabitList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        dynamicHabitList = gson.fromJson(json,type);


        if (dynamicHabitList == null) {
            dynamicHabitList = new ArrayList<DynamicHabit>();
        }
        else {
            String x = "just a placer";
        }

        //Turning ArrayList into JSON and then applying.
        json = gson.toJson(dynamicHabitList);
        dynamicHabitEditor.putString("personalHabitList", json);
        dynamicHabitEditor.apply();
    }

}
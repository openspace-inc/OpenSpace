package com.example.iaso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.PersonalPage.dataStorage;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

//This page is to bring in all data from a specific project and display that data in a meaningful way through libraries
public class Analytics extends AppCompatActivity {

    String name1;
    String project_description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        name1 = getIntent().getStringExtra("project_name");
        project_description = getIntent().getStringExtra("project_description");
        TextView name = findViewById(R.id.name);
        name.setText(name1);
        TextView description = findViewById(R.id.description);
        description.setText(project_description);

        ImageButton exitButton = findViewById(R.id.backbutton234);
        exitButton.setOnClickListener(view -> {
            Intent b = new Intent(Analytics.this, PersonalPage.class);
            startActivity(b);
        });

        totalHours();
        //totalHoursThisWeek();

        //set up recyclerview
        //dataPull();
    }

    //opens sharedpref for data storage and returns the arraylist
    ArrayList<dataStorage> openSharedPref(){
        ArrayList<dataStorage> storage = new ArrayList<>();

        //Create userStorage arraylst for the app
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
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

        return storage;
    }

    //not in use currently - supposed to store sharedpref and close it
    void applySharedPref(ArrayList<dataStorage> dataArrayList){
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor dataStorage = userStorage.edit();

        //get ArrayList from shared preferences
        String json = userStorage.getString("userStorageList",null);
        Gson gson = new Gson();

        //Turning ArrayList into JSON and then applying.
        json = gson.toJson(dataArrayList);
        dataStorage.putString("userStorageList", json);
        dataStorage.apply();
    }

    //Displays total hours worked toward this project
    void totalHours(){
        //initalize shared preferences
        ArrayList<dataStorage> storage = openSharedPref();
        double hours = 0;

        for (dataStorage x : storage){
            if (x.getName().equals(name1)) {
                hours += x.getHours();
            }
        }

        TextView totalHours = findViewById(R.id.amountOfHours);
        totalHours.setText(hours + "");
    }
}
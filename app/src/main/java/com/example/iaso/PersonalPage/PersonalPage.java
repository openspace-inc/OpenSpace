package com.example.iaso.PersonalPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.AddDynamicHabit;
import com.example.iaso.Analytics;
import com.example.iaso.BottomNavigationHelper;
import com.example.iaso.BrownianStockManager;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;
import com.example.iaso.ToDoList.RecyclerViewInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

//This page is to display all personal projects the user has through a recyclerview interface
public class PersonalPage extends AppCompatActivity implements RecyclerViewInterface {


    //used to run animation only once
    private static boolean hasPlayedAnimation = false;
    CardView achievementCard;

    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>(); //Stores List Of Projects
    SharedPreferences dynamicHabits;

    public ArrayList<dataStorage> dataStorageList = new ArrayList<dataStorage>(); //Stores Recorded Data Of Projects
    SharedPreferences dataStorage;

    RecyclerView displayPersonalHabits;

    ImageButton exitButton;
    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        emptyText = findViewById(R.id.textView19);

        TextView Title = findViewById(R.id.PersonalPageHeader);
        String title = "Your Projects";
        Title.setText(title);

        //set the achievement cardview to invisible
        achievementCard = findViewById(R.id.AchievementPage);
        achievementCard.setVisibility(View.INVISIBLE);


        //Initialize recycler view
        displayPersonalHabits = findViewById(R.id.personalHabitDisplayRecyclerView);

        if (!hasPlayedAnimation){
            Animation upwardsFade = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
            displayPersonalHabits.startAnimation(upwardsFade);

            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
            fadeInAnimation.setStartOffset(300);
            Title.startAnimation(fadeInAnimation);

            hasPlayedAnimation = true;
        }

        //Ensure Brownian stock data is up to date before displaying
        BrownianStockManager.updateAll(this);

        //Recieve sharedpref for projects - apply to recyclerview (auto set up dynamichabits sharedpref)
        setUpPersonalHabits();

        //Press to add habit
        ImageButton addContract = findViewById(R.id.addHabitButton);
        Intent b = new Intent(this, AddDynamicHabit.class);
        addContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(b);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        //Exit the activity
        exitButton = findViewById(R.id.exitButtonForPersonalPage);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent b = new Intent(PersonalPage.this, MainActivity.class);
                startActivity(b);
            }
        });

        // Setup bottom navigation bar
        BottomNavigationHelper.setupBottomNavigation(this, R.id.bottom_nav_include, PersonalPage.class);
    }

    //Add a data entry to the dataStorage sharedPref
    private void addToDataStorage(dataStorage newData) {
        dataStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE); //brought in file
        SharedPreferences.Editor dataEditor = dataStorage.edit(); //allowed editing of file

        String json = dataStorage.getString("userStorageList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        dataStorageList = gson.fromJson(json,type); //filled arraylist with stored data

        //Debugging control: (displays data stored)
        for (dataStorage x : dataStorageList) {
            Log.d("ArrayListCheck", "Person Name: " + x.getName()); // Appears in Logcat
            Log.d("ArrayListCheck", "Person Type: " + x.getType());
            Log.d("ArrayListCheck", "Person Hours: " + x.getHours());
            Log.d("ArrayListCheck", "Date:" + x.getDate());
        }

        //Prevents runtime errors
        if (dataStorageList == null) {
            dataStorageList = new ArrayList<dataStorage>();
        }
        else {
            String x = "just a placer";
        }

        //Addition of data entry to storage
        dataStorageList.add(newData);
        String updatedJson = gson.toJson(dataStorageList);

        achievementCard.setVisibility(View.VISIBLE);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation.setStartOffset(300);
        achievementCard.startAnimation(fadeInAnimation);
        TextView minutesText = findViewById(R.id.minutesText);
        minutesText.setText(String.valueOf(newData.getHours()));

        //Save the updated JSON string back into SharedPreferences
        dataEditor.putString("userStorageList", updatedJson);
        dataEditor.apply();

        BrownianStockManager.onMinutesLogged(this, newData.getName());

        //Debugging confirm that the habit has been stored
        Toast.makeText(getApplicationContext(), "Success. added the data", Toast.LENGTH_SHORT).show();
    }

    //Open sharedpref for the projects and apply to recyclerview
    private void setUpPersonalHabits() {
        dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS);
        String json = dynamicHabits.getString("personalHabitList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        dynamicHabitList = gson.fromJson(json,type);

        if (dynamicHabitList == null || dynamicHabitList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No Projects To Display. Build one using the square button above");
            displayPersonalHabits.setVisibility(View.INVISIBLE);
        }
        else {
            emptyText.setVisibility(View.INVISIBLE);
            PersonalHabit_RecyclerViewAdapter recyclerViewAdapter = new PersonalHabit_RecyclerViewAdapter(this, dynamicHabitList, this);
            displayPersonalHabits.setAdapter(recyclerViewAdapter);
            displayPersonalHabits.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    //Upon long pressing project, call the analytics page
    void callAnalyticsClass(int position){
        Intent b = new Intent(PersonalPage.this, Analytics.class);
        b.putExtra("project_name", dynamicHabitList.get(position).getName3());
        b.putExtra("stock_name", dynamicHabitList.get(position).getStockSymbol());
        b.putExtra("project_description", dynamicHabitList.get(position).getDescription());
        startActivity(b);
    }

    //Call storage of data entry
    @Override
    public void onItemClick(int position) {
        if (dynamicHabitList == null) {
            Toast.makeText(getApplicationContext(), "No projects to display", Toast.LENGTH_SHORT).show();
        }
        else {
            //Adds new entry to dataStorage ArrayList
            String name = dynamicHabitList.get(position).getName3();
            int time = dynamicHabitList.get(position).getTime();

            dataStorage newData = new dataStorage(name, "Project", time);
            addToDataStorage(newData);
        }
    }

    //Call analytics page
    @Override
    public void onItemLongClick(int position) {
         callAnalyticsClass(position);
    }

    //transfer to hub page
    public void goToHub(View view) {
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }

    //this piece of code doesn't work
    public void exitAchievementText(View view){
        achievementCard.setVisibility(View.INVISIBLE);
    }

}
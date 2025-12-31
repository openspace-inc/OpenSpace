package com.example.iaso.Home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.example.iaso.Health.Health;
import com.example.iaso.Introduction.WelcomeActivity;
import com.example.iaso.BottomNavigationHelper;
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.PersonalPage.dataStorage;
import com.example.iaso.Projects;
import com.example.iaso.R;
import com.example.iaso.ToDoList.TaskLister;
import com.example.iaso.workhorse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.ticker.TickerView;
import com.robinhood.ticker.TickerUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    LinearLayout projectContainer;

    SparkView timeInvestedSparkView;
    TickerView totalMinutesTickerView;
    android.widget.RadioGroup timeRangeGroup;
    Map<Integer, Double> aggregatedTimeData = new HashMap<>();
    List<Float> currentGraphData = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Open IntroActivity On First Run
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show start activity
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_LONG)
                    .show();
        }

        //set pro text to invisible as default
        CardView welcomeToPro = findViewById(R.id.welcomeToPro);
        welcomeToPro.setVisibility(View.INVISIBLE);

        //Profile Page Setup
        ImageButton profile = findViewById(R.id.profileIcon);
        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, workhorse.class);
                startActivity(intent);
            }
        });

        // Setup bottom navigation bar
        BottomNavigationHelper.setupBottomNavigation(this, R.id.bottom_nav_include, MainActivity.class);

        //Set up horizontal list of projects
        projectContainer = findViewById(R.id.projectContainer);

        ImageButton featuredProjectButton = findViewById(R.id.featuredProjectButton);
        featuredProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Projects.class);
            startActivity(intent);
        });
        loadPersonalHabits();
        populateProjectRow();

        // Setup time invested spark graph and ticker
        timeInvestedSparkView = findViewById(R.id.timeInvestedSparkView);
        totalMinutesTickerView = findViewById(R.id.totalMinutesTickerView);
        timeRangeGroup = findViewById(R.id.timeRangeGroup);

        // Initialize ticker view with number list and font
        totalMinutesTickerView.setCharacterLists(TickerUtils.provideNumberList());
        Typeface neuehaas45 = getResources().getFont(R.font.neuehaas45);
        totalMinutesTickerView.setTypeface(neuehaas45);

        // Set initial value to 0 so it animates from 0 on first load
        totalMinutesTickerView.setText("0 min");

        // Load and aggregate time data
        loadTimeInvestedData();

        // Set default selection to 7 days
        timeRangeGroup.check(R.id.timeRange7d);
        updateTimeInvestedGraph(7);

        // Setup button listeners
        timeRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.timeRange7d) {
                updateTimeInvestedGraph(7);
            } else if (checkedId == R.id.timeRange2w) {
                updateTimeInvestedGraph(14);
            } else if (checkedId == R.id.timeRange1m) {
                updateTimeInvestedGraph(30);
            } else if (checkedId == R.id.timeRange3m) {
                updateTimeInvestedGraph(90);
            } else if (checkedId == R.id.timeRange6m) {
                updateTimeInvestedGraph(180);
            } else if (checkedId == R.id.timeRange12m) {
                updateTimeInvestedGraph(365);
            } else if (checkedId == R.id.timeRangeAll) {
                updateTimeInvestedGraph(-1); // -1 means all time
            }
        });
    }

    private void loadPersonalHabits() {
        dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS);
        String json = dynamicHabits.getString("personalHabitList", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
            dynamicHabitList = gson.fromJson(json, type);
        }
    }

    private void populateProjectRow() {
        if (dynamicHabitList == null || dynamicHabitList.isEmpty()) {
            return;
        }

        for (DynamicHabit habit : dynamicHabitList) {
            int imageRes = getResources().getIdentifier(habit.getImageName(), "drawable", getPackageName());

            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(125), dpToPx(125));
            params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            frame.setLayoutParams(params);
            frame.setBackgroundResource(R.drawable.story_ring);

            ImageButton button = new ImageButton(this);
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            btnParams.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            button.setLayoutParams(btnParams);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(imageRes).circleCrop().into(button);
            button.setOnClickListener(v ->
                    Toast.makeText(getApplicationContext(), habit.getDescription(), Toast.LENGTH_SHORT).show()
            );

            frame.addView(button);
            projectContainer.addView(frame);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    //Create arrayList where habits are stored in sharedpreferences.
    public void fillDynamicHabits() {
        dynamicHabits = getSharedPreferences("DynamicHabits", Context.MODE_PRIVATE);
        SharedPreferences.Editor dynamicHabitEditor = dynamicHabits.edit();

        DynamicHabit pullUpHabit = new DynamicHabit("Pull Ups", 0, "Fitness", "Strength Your UpperBack Through Pullups", 5, 15, "calmshades", 0);
        DynamicHabit pullUpHabit2 = new DynamicHabit("Pull Ups2", 4, "Fitness", "Strength Your UpperBack Through Pullups23", 256, 15, "calmshades", 0);
        dynamicHabitList.add(pullUpHabit);
        dynamicHabitList.add(pullUpHabit2);

        Gson gson = new Gson();

        String json = gson.toJson(dynamicHabitList);
        dynamicHabitEditor.putString("dynamicHabitList", json);
        dynamicHabitEditor.apply();
    }

    //Send User To Health Activity
    public void goToHealth(View v){
        Intent b = new Intent(this, Health.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }

    public void goToWelcomeTESTER(View v){
        Intent b = new Intent(this, WelcomeActivity.class);
        startActivity(b);
    }

    public void goToDoList(View v){
        Intent b = new Intent(this, TaskLister.class);
        startActivity(b);
    }

    public void goToPersonalPage(View v){
        Intent b = new Intent(this, PersonalPage.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }


    //Send User To Exercise Activity
    public void goToExercise(View v){
        Intent b = new Intent(this, PersonalPage.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }

    //Send User To Store Activity
    public void goToStore(View v){
        Intent b = new Intent(this, Store.class);
        startActivity(b);
    }

    public void goToProjects(View v){
        Intent b = new Intent(this, Projects.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }


    public void exitProText(View view){
        CardView welcome = findViewById(R.id.welcomeToPro);
        welcome.setVisibility(View.GONE);
    }

    private void loadTimeInvestedData() {
        // Clear existing data
        aggregatedTimeData.clear();

        // Load userStorageList from SharedPreferences
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_MULTI_PROCESS);
        String json = userStorage.getString("userStorageList", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
            ArrayList<dataStorage> storageList = gson.fromJson(json, type);

            // Aggregate time by day
            for (dataStorage entry : storageList) {
                int day = entry.getDate();
                double hours = entry.getHours();

                if (aggregatedTimeData.containsKey(day)) {
                    aggregatedTimeData.put(day, aggregatedTimeData.get(day) + hours);
                } else {
                    aggregatedTimeData.put(day, hours);
                }
            }
        }
    }

    private void updateTimeInvestedGraph(int daysToShow) {
        // Get current day of year
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_YEAR);

        // Prepare data for the selected time range
        List<Float> graphData = new ArrayList<>();

        if (daysToShow == -1) {
            // Show all time data
            // Find the earliest day in the data
            int minDay = currentDay;
            for (int day : aggregatedTimeData.keySet()) {
                if (day < minDay) {
                    minDay = day;
                }
            }

            // Calculate how many days to show
            daysToShow = currentDay - minDay + 1;
        }

        // Build graph data from (currentDay - daysToShow + 1) to currentDay
        int startDay = currentDay - daysToShow + 1;

        for (int i = 0; i < daysToShow; i++) {
            int day = startDay + i;

            // Handle year wrap-around (day could be negative if we go back past Jan 1)
            if (day <= 0) {
                // Previous year - assume 365 days for simplicity
                day = 365 + day;
            }

            Double hours = aggregatedTimeData.get(day);
            graphData.add(hours != null ? hours.floatValue() : 0f);
        }

        // Store current graph data
        currentGraphData = new ArrayList<>(graphData);

        // Calculate total minutes from the graph data
        float totalHours = 0f;
        for (Float hours : graphData) {
            totalHours += hours;
        }
        int totalMinutes = Math.round(totalHours);

        // Update ticker view with animated transition
        totalMinutesTickerView.setText(String.valueOf(totalMinutes) + " min");

        // Create and set the adapter
        SparkAdapter adapter = new SparkAdapter() {
            @Override
            public int getCount() {
                return graphData.size();
            }

            @Override
            public Object getItem(int index) {
                return graphData.get(index);
            }

            @Override
            public float getY(int index) {
                return graphData.get(index);
            }
        };

        timeInvestedSparkView.setAdapter(adapter);
    }

}



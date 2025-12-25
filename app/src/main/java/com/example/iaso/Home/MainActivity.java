package com.example.iaso.Home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Back Button For Fragments
    ImageButton back;

    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    LottieAnimationView dynamicLogo2;
    LinearLayout projectContainer;

    // Daily Investment Spark Graph
    private SparkView dailySparkView;
    private TextView dailyTotalTime;
    private TextView dailyInvestmentLabel;
    private RadioGroup dailyRangeGroup;
    private RecyclerView projectTimeRecyclerView;
    private ProjectTimeAdapter projectTimeAdapter;
    private ArrayList<dataStorage> userStorageList = new ArrayList<>();
    private DailyRange currentDailyRange = DailyRange.DAY_1;

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

        //Create Animation of ImageButtons
        ImageButton toDoList = findViewById(R.id.todolistButton);
        ImageButton storeButton = findViewById(R.id.storeButton);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation fadeInAnimation3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        fadeInAnimation.setStartOffset(300);
        fadeInAnimation3.setStartOffset(900);

        toDoList.startAnimation(fadeInAnimation);
        storeButton.startAnimation(fadeInAnimation3);

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

        //Create Habits

        //Set IntroText
        TextView introText = findViewById(R.id.generatedText);
        setIntroText(introText);

        //Set Dynamic Logo(old gif file)
        //ImageView dynamicLogoIaso = findViewById(R.id.dynamicLogo);
        //Glide.with(this).asGif().load(R.drawable.iasodyanmiclogo).into(dynamicLogoIaso);

        //Set Dynamic Logo2
        dynamicLogo2 = findViewById(R.id.dynamicLogo);
        dynamicLogo2.setAnimation(R.raw.iasodynamiclogo);
        dynamicLogo2.setRepeatCount(LottieDrawable.INFINITE);
        dynamicLogo2.playAnimation();

        //Set BackButton
        back = findViewById(R.id.backButtonForFragments);
        back.setVisibility(View.GONE);

        //Set up horizontal list of projects
        projectContainer = findViewById(R.id.projectContainer);

        ImageButton featuredProjectButton = findViewById(R.id.featuredProjectButton);
        featuredProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Projects.class);
            startActivity(intent);
        });
        loadPersonalHabits();
        populateProjectRow();

        // Initialize Daily Investment Spark Graph
        setupDailyInvestmentGraph();
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


    public void goBack(View v){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, PersonalHomePageFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack("name")
                .commit();

        back.setVisibility(View.GONE);
    }

    //Set Introduction text based on time of day
    public void setIntroText(View v){
        //Get Data
        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String name = userData.getString("name","");
        Boolean firstRun = userData.getBoolean("firstRun", false);
        String timingText = "";

        //Get only first name
        String firstName = "";
        int firstSpaceIndex = name.indexOf(" ");

        if (firstSpaceIndex != -1) {
            firstName= name.substring(0, firstSpaceIndex);
        } else {
            firstName = name;
        }
        //Get Time
        Date thisDate2 = new Date();
        SimpleDateFormat dateForm2 = new SimpleDateFormat("HH");
        String stringTime = dateForm2.format(thisDate2);
        int currentTime = Integer.parseInt(stringTime);

        //Generate random number
        Random random = new Random();
        int randomNumber = random.nextInt(4 - 1 + 1) + 1;

        //Get data to test if it is the first run
        if(firstRun){
            CardView welcome = findViewById(R.id.welcomeToPro);
            welcome.setVisibility(View.VISIBLE);
            timingText = "Welcome to Iaso";
            SharedPreferences.Editor editor = userData.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
        }
        //Decide what time it is and set randomized text based on it.
        else{
            if((currentTime >= 4) && (currentTime < 10)){
                if(randomNumber == 1){
                    timingText = "Good Morning";
                }
                else if(randomNumber == 2){
                    timingText = "A New Day Awaits";
                }
                else if (randomNumber == 3){
                    timingText = "Rise and Shine";
                }
                else{
                    timingText = "Bonjour";
                }

            }
            else if ((currentTime >= 11) && (currentTime < 19)){
                if(randomNumber == 1){
                    timingText = "Good Afternoon";
                }
                else if(randomNumber == 2){
                    timingText = "One Step At A Time";
                }
                else if (randomNumber == 3){
                    timingText = "Keep Going";
                }
                else{
                    timingText = "It Will Be Worth It";
                }

            }
            else {
                if(randomNumber == 1){
                    timingText = "Good Night";
                }
                else if(randomNumber == 2){
                    timingText = "Almost There";
                }
                else if (randomNumber == 3){
                    timingText = "Times Ticking";
                }
                else{
                    timingText = "Have A Nice Night";
                }
            }
        }

        //Combine Text
        String generatedText = timingText + ", " + firstName;

        //Set Calculations To Text In Display
        TextView textDisplay;
        textDisplay = findViewById(R.id.generatedText);
        textDisplay.setText(generatedText);

        //Gradient Color
        TextPaint paint = textDisplay.getPaint();
        float width = paint.measureText(generatedText);

        Shader textShader = new LinearGradient(0, 0, width, textDisplay.getTextSize(),
                new int[]{
                        Color.parseColor("#de6262"),
                        Color.parseColor("#ffb88c"),
                }, null, Shader.TileMode.CLAMP);
        textDisplay.getPaint().setShader(textShader);

    }

    public void exitProText(View view){
        CardView welcome = findViewById(R.id.welcomeToPro);
        welcome.setVisibility(View.GONE);
    }

    private void setupDailyInvestmentGraph() {
        dailySparkView = findViewById(R.id.dailySparkView);
        dailyTotalTime = findViewById(R.id.dailyTotalTime);
        dailyInvestmentLabel = findViewById(R.id.dailyInvestmentLabel);
        dailyRangeGroup = findViewById(R.id.dailyRangeGroup);
        projectTimeRecyclerView = findViewById(R.id.projectTimeRecyclerView);

        // Load user storage data
        loadUserStorageData();

        // Setup RecyclerView
        projectTimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectTimeAdapter = new ProjectTimeAdapter(this, new ArrayList<>());
        projectTimeRecyclerView.setAdapter(projectTimeAdapter);

        // Setup range buttons
        setupDailyRangeButtons();

        // Initial update with default range (1 Day)
        dailyRangeGroup.check(R.id.dailyRange1d);
        updateDailyInvestmentForRange(currentDailyRange);
    }

    private void loadUserStorageData() {
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        String json = userStorage.getString("userStorageList", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
            userStorageList = gson.fromJson(json, type);
            if (userStorageList == null) {
                userStorageList = new ArrayList<>();
            }
        }
    }

    private void setupDailyRangeButtons() {
        if (dailyRangeGroup == null) {
            return;
        }

        dailyRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            DailyRange selected = mapDailyRangeFromId(checkedId);
            if (selected != null && selected != currentDailyRange) {
                currentDailyRange = selected;
                updateDailyInvestmentForRange(currentDailyRange);
            }
        });
    }

    private DailyRange mapDailyRangeFromId(int checkedId) {
        if (checkedId == R.id.dailyRange1d) {
            return DailyRange.DAY_1;
        } else if (checkedId == R.id.dailyRange1w) {
            return DailyRange.WEEK_1;
        } else if (checkedId == R.id.dailyRange1m) {
            return DailyRange.MONTH_1;
        } else if (checkedId == R.id.dailyRange3m) {
            return DailyRange.MONTH_3;
        } else if (checkedId == R.id.dailyRange1y) {
            return DailyRange.YEAR_1;
        } else if (checkedId == R.id.dailyRangeAll) {
            return DailyRange.ALL;
        }
        return null;
    }

    private void updateDailyInvestmentForRange(DailyRange range) {
        // Get the current date info
        Calendar now = Calendar.getInstance();
        int currentDayOfYear = now.get(Calendar.DAY_OF_YEAR);

        // Calculate the number of days to look back
        int daysToLookBack = range == DailyRange.ALL ? 365 : range.getDays();

        // Build a set of valid day numbers for the range
        ArrayList<Integer> validDays = new ArrayList<>();
        for (int i = 0; i < daysToLookBack; i++) {
            int day = currentDayOfYear - i;
            if (day < 1) {
                day += 365; // Handle year wrap-around
            }
            validDays.add(day);
        }

        // Filter and aggregate data by project
        Map<String, ProjectTimeData> projectTimeMap = new HashMap<>();

        // Group data by day for spark graph
        Map<Integer, Double> dailyTotals = new HashMap<>();

        double totalMinutes = 0;

        for (dataStorage entry : userStorageList) {
            if (entry == null) continue;

            int entryDay = entry.getDate();
            String projectName = entry.getName();

            // Check if this entry falls within our valid days
            boolean inRange = (range == DailyRange.ALL) || validDays.contains(entryDay);

            if (inRange) {
                double minutes = entry.getHours();
                totalMinutes += minutes;

                // Aggregate by project - sum up all time for this project in the range
                if (projectTimeMap.containsKey(projectName)) {
                    projectTimeMap.get(projectName).addMinutes(minutes);
                } else {
                    String imageName = getImageNameForProject(projectName);
                    projectTimeMap.put(projectName, new ProjectTimeData(projectName, imageName, minutes));
                }

                // Aggregate by day for spark graph
                if (dailyTotals.containsKey(entryDay)) {
                    dailyTotals.put(entryDay, dailyTotals.get(entryDay) + minutes);
                } else {
                    dailyTotals.put(entryDay, minutes);
                }
            }
        }

        // Update label based on range
        updateRangeLabel(range);

        // Update total time display
        int totalMins = (int) totalMinutes;
        if (totalMins >= 60) {
            int hours = totalMins / 60;
            int mins = totalMins % 60;
            dailyTotalTime.setText(hours + "h " + mins + "m");
        } else {
            dailyTotalTime.setText(totalMins + " mins");
        }

        // Build spark data points - oldest to newest (left to right)
        ArrayList<Float> sparkData = new ArrayList<>();
        for (int i = daysToLookBack - 1; i >= 0; i--) {
            int day = currentDayOfYear - i;
            if (day < 1) day += 365;
            Double value = dailyTotals.get(day);
            sparkData.add(value != null ? value.floatValue() : 0f);
        }

        // Update spark view
        if (dailySparkView != null) {
            dailySparkView.setAdapter(new DailySparkAdapter(sparkData));
        }

        // Update project list with all projects that have time in this range
        ArrayList<ProjectTimeData> projectList = new ArrayList<>(projectTimeMap.values());
        projectTimeAdapter.updateData(projectList);
    }

    private void updateRangeLabel(DailyRange range) {
        if (dailyInvestmentLabel == null) return;

        switch (range) {
            case DAY_1:
                dailyInvestmentLabel.setText("Today's Investment");
                break;
            case WEEK_1:
                dailyInvestmentLabel.setText("This Week's Investment");
                break;
            case MONTH_1:
                dailyInvestmentLabel.setText("This Month's Investment");
                break;
            case MONTH_3:
                dailyInvestmentLabel.setText("Last 3 Months Investment");
                break;
            case YEAR_1:
                dailyInvestmentLabel.setText("This Year's Investment");
                break;
            case ALL:
                dailyInvestmentLabel.setText("Total Investment");
                break;
        }
    }

    private String getImageNameForProject(String projectName) {
        for (DynamicHabit habit : dynamicHabitList) {
            if (habit.getName3().equals(projectName)) {
                return habit.getImageName();
            }
        }
        return "orb2";
    }

    private enum DailyRange {
        DAY_1("1D", 1),
        WEEK_1("1W", 7),
        MONTH_1("1M", 30),
        MONTH_3("3M", 90),
        YEAR_1("1Y", 365),
        ALL("All", -1);

        private final String label;
        private final int days;

        DailyRange(String label, int days) {
            this.label = label;
            this.days = days;
        }

        int getDays() {
            return days;
        }

        String getLabel() {
            return label;
        }
    }

    private class DailySparkAdapter extends SparkAdapter {
        private final ArrayList<Float> data;

        DailySparkAdapter(ArrayList<Float> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int index) {
            return data.get(index);
        }

        @Override
        public float getY(int index) {
            return data.get(index);
        }
    }

}



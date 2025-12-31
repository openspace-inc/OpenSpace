package com.example.iaso.Home;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iaso.BottomNavigationHelper;
import com.example.iaso.Health.Health;
import com.example.iaso.Introduction.WelcomeActivity;
import com.example.iaso.Milestone;
import com.example.iaso.MilestoneStorage;
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
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    LinearLayout projectContainer;

    SparkView timeInvestedSparkView;
    TickerView totalMinutesTickerView;
    android.widget.RadioGroup timeRangeGroup;
    RecyclerView habitTimeRecyclerView;
    HabitTimeAdapter habitTimeAdapter;
    Map<Integer, Double> aggregatedTimeData = new HashMap<>();
    List<Float> currentGraphData = new ArrayList<>();
    int currentDaysToShow = 7;

    // Habit detail views
    androidx.constraintlayout.widget.ConstraintLayout habitDetailContainer;
    TextView habitDetailName;
    TextView habitDetailMilestone;
    TextView habitDetailCompletionTime;
    androidx.constraintlayout.widget.ConstraintLayout hubContainer;

    // Timeline views
    ImageView timelineImage;
    ImageView timelineArrow;
    TextView timelineDaysNumber;
    TextView timelineDaysLabel;
    TextView timelineUpgradeMessage;

    // Project indicator
    ImageView projectIndicator;
    ImageButton featuredProjectButton;
    HorizontalScrollView projectScrollView;
    List<View> projectButtons = new ArrayList<>();
    View currentSelectedButton = null;
    int currentSelectedProjectIndex = -1;

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

        // Setup habit detail views before featuredProjectButton
        habitDetailContainer = findViewById(R.id.habitDetailContainer);
        habitDetailName = findViewById(R.id.habitDetailName);
        habitDetailMilestone = findViewById(R.id.habitDetailMilestone);
        habitDetailCompletionTime = findViewById(R.id.habitDetailCompletionTime);
        hubContainer = findViewById(R.id.Hub);

        // Setup timeline views
        timelineImage = findViewById(R.id.timelineImage);
        timelineArrow = findViewById(R.id.timelineArrow);
        timelineDaysNumber = findViewById(R.id.timelineDaysNumber);
        timelineDaysLabel = findViewById(R.id.timelineDaysLabel);
        timelineUpgradeMessage = findViewById(R.id.timelineUpgradeMessage);

        //Set up horizontal list of projects
        projectContainer = findViewById(R.id.projectContainer);
        projectIndicator = findViewById(R.id.projectIndicator);
        projectScrollView = findViewById(R.id.projectScrollView);

        // Add scroll listener to update indicator position while scrolling
        projectScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Update indicator position without animation during scroll
            if (currentSelectedButton != null) {
                updateIndicatorPosition();
            }
        });

        featuredProjectButton = findViewById(R.id.featuredProjectButton);
        featuredProjectButton.setOnClickListener(v -> {
            // Check if habit detail view is currently shown
            if (habitDetailContainer != null && habitDetailContainer.getVisibility() == View.VISIBLE) {
                // Restore main view
                showMainView();
            } else {
                // Navigate to workhorse activity
                Intent intent = new Intent(MainActivity.this, workhorse.class);
                startActivity(intent);
            }

            // Always animate indicator to featuredProjectButton when clicked
            currentSelectedButton = featuredProjectButton;
            animateIndicatorToButton(featuredProjectButton, 0, true);
        });

        loadPersonalHabits();
        populateProjectRow();

        // Initialize indicator position to featuredProjectButton after layout
        featuredProjectButton.post(() -> {
            currentSelectedButton = featuredProjectButton;
            animateIndicatorToButton(featuredProjectButton, 0, false);
        });

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

        // Setup RecyclerView
        habitTimeRecyclerView = findViewById(R.id.habitTimeRecyclerView);
        habitTimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitTimeAdapter = new HabitTimeAdapter(this);
        habitTimeRecyclerView.setAdapter(habitTimeAdapter);

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

        // Clear existing buttons list
        projectButtons.clear();

        int index = 0;
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

            // Store button reference
            projectButtons.add(frame);

            // Set click listener to show habit details and animate indicator
            final int buttonIndex = index + 1; // +1 because featuredProjectButton is index 0
            button.setOnClickListener(v -> {
                showHabitDetail(habit);
                currentSelectedButton = frame;
                animateIndicatorToButton(frame, buttonIndex, true);
            });

            frame.addView(button);
            projectContainer.addView(frame);
            index++;
        }
    }

    /**
     * Shows the habit detail view with milestone information
     * @param habit The DynamicHabit to display details for
     */
    private void showHabitDetail(DynamicHabit habit) {
        // Hide Hub and RecyclerView
        if (hubContainer != null) {
            hubContainer.setVisibility(View.GONE);
        }
        if (habitTimeRecyclerView != null) {
            habitTimeRecyclerView.setVisibility(View.GONE);
        }

        // Show habit detail container
        if (habitDetailContainer != null) {
            habitDetailContainer.setVisibility(View.VISIBLE);
        }

        // Set habit name
        if (habitDetailName != null) {
            habitDetailName.setText(habit.getName3());
        }

        // Load milestones for this habit
        MilestoneStorage milestoneStorage = new MilestoneStorage(this);
        List<Milestone> milestones = milestoneStorage.getMilestonesForHabit(habit.getName3());

        // Set milestone text
        if (habitDetailMilestone != null) {
            if (milestones.isEmpty()) {
                habitDetailMilestone.setText("no milestones exist, upgrade to pro to generate.");
            } else {
                // Show first milestone
                Milestone firstMilestone = milestones.get(0);
                habitDetailMilestone.setText(firstMilestone.getName());
            }
        }

        // Calculate and set total completion time
        int totalCompletionDays = 0;
        if (habitDetailCompletionTime != null) {
            if (milestones.isEmpty()) {
                habitDetailCompletionTime.setText("0 days");
            } else {
                for (Milestone milestone : milestones) {
                    totalCompletionDays += milestone.getDays();
                }
                habitDetailCompletionTime.setText(totalCompletionDays + " days");
            }
        }

        // Setup and animate timeline
        setupTimeline(habit, totalCompletionDays);
    }

    /**
     * Shows the main view (Hub and RecyclerView) and hides habit detail view
     */
    private void showMainView() {
        // Show Hub and RecyclerView
        if (hubContainer != null) {
            hubContainer.setVisibility(View.VISIBLE);
        }
        if (habitTimeRecyclerView != null) {
            habitTimeRecyclerView.setVisibility(View.VISIBLE);
        }

        // Hide habit detail container
        if (habitDetailContainer != null) {
            habitDetailContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up and animates the timeline visualization for the selected habit.
     * Calculates the progress percentage based on days elapsed since creation.
     *
     * @param habit The DynamicHabit to display timeline for
     * @param totalCompletionDays Total planned days to complete the habit (from milestones)
     */
    private void setupTimeline(DynamicHabit habit, int totalCompletionDays) {
        // Check if we have all necessary data
        long creationDate = habit.getCreationDate();
        boolean hasValidData = creationDate > 0 && totalCompletionDays > 0;

        if (!hasValidData) {
            // Show fallback state: timeline at start, upgrade message
            if (timelineUpgradeMessage != null) {
                timelineUpgradeMessage.setVisibility(View.VISIBLE);
            }
            if (timelineDaysNumber != null) {
                timelineDaysNumber.setText("0");
            }
            // Position arrow at the very left (0% progress)
            if (timelineArrow != null) {
                timelineArrow.post(() -> animateTimelineArrow(0f));
            }
            return;
        }

        // Hide upgrade message when we have valid data
        if (timelineUpgradeMessage != null) {
            timelineUpgradeMessage.setVisibility(View.GONE);
        }

        // Calculate days elapsed since project creation
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - creationDate;
        int daysElapsed = (int) (elapsedMillis / (1000 * 60 * 60 * 24)); // Convert milliseconds to days

        // Calculate progress percentage: (days elapsed / total completion days) * 100
        // Cap at 100% maximum
        float calculatedPercentage = ((float) daysElapsed / (float) totalCompletionDays) * 100f;
        final float progressPercentage = Math.min(calculatedPercentage, 100f);

        // Update timeline days display
        if (timelineDaysNumber != null) {
            timelineDaysNumber.setText(String.valueOf(totalCompletionDays));
        }

        // Animate the arrow to the calculated position after layout is complete
        if (timelineArrow != null && timelineImage != null) {
            timelineArrow.post(() -> animateTimelineArrow(progressPercentage));
        }
    }

    /**
     * Animates the timeline arrow from the left to its target position based on progress percentage.
     *
     * @param progressPercentage The progress percentage (0-100) indicating how far along the timeline the arrow should be
     */
    private void animateTimelineArrow(float progressPercentage) {
        if (timelineArrow == null || timelineImage == null) {
            return;
        }

        // Get the width of the timeline image (75% of container width)
        int timelineWidth = timelineImage.getWidth();
        if (timelineWidth == 0) {
            // Layout not complete yet, try again
            timelineArrow.post(() -> animateTimelineArrow(progressPercentage));
            return;
        }

        // Calculate target X position within the timeline
        // At 0%: arrow is at the left edge of timeline
        // At 100%: arrow is at the right edge of timeline
        // Account for arrow width so it doesn't go past the timeline edge
        int arrowWidth = timelineArrow.getWidth();
        float maxTranslation = timelineWidth - arrowWidth;
        float targetTranslationX = (progressPercentage / 100f) * maxTranslation;

        // Reset arrow to start position (left edge)
        timelineArrow.setTranslationX(0f);

        // Animate arrow from left to target position with smooth easing
        ObjectAnimator animator = ObjectAnimator.ofFloat(timelineArrow, "translationX", 0f, targetTranslationX);
        animator.setDuration(800); // 800ms for smooth animation
        animator.setInterpolator(new DecelerateInterpolator(1.5f)); // Ease out with deceleration
        animator.start();
    }

    /**
     * Animates the project indicator to center underneath the specified button
     * @param targetButton The button to position the indicator under
     * @param index The index of the button (0 for featuredProjectButton, 1+ for dynamic habits)
     * @param animate Whether to animate the movement (false for initial positioning)
     */
    private void animateIndicatorToButton(View targetButton, int index, boolean animate) {
        if (projectIndicator == null || targetButton == null) {
            return;
        }

        // Make indicator visible if this is a new selection
        if (currentSelectedProjectIndex != index) {
            projectIndicator.setVisibility(View.VISIBLE);
            currentSelectedProjectIndex = index;
        }

        // Calculate the center X position of the target button
        int[] buttonLocation = new int[2];
        targetButton.getLocationOnScreen(buttonLocation);

        int[] indicatorLocation = new int[2];
        projectIndicator.getLocationOnScreen(indicatorLocation);

        // Calculate the translation needed to center the indicator under the button
        float buttonCenterX = buttonLocation[0] + (targetButton.getWidth() / 2f);
        float indicatorCenterX = indicatorLocation[0] + (projectIndicator.getWidth() / 2f);
        float targetTranslationX = projectIndicator.getTranslationX() + (buttonCenterX - indicatorCenterX);

        if (animate) {
            // Animate with smooth decelerate interpolator and spline-like easing
            ObjectAnimator animator = ObjectAnimator.ofFloat(projectIndicator, "translationX", targetTranslationX);
            animator.setDuration(350);
            animator.setInterpolator(new DecelerateInterpolator(1.5f));
            animator.start();
        } else {
            // Set position immediately without animation
            projectIndicator.setTranslationX(targetTranslationX);
        }
    }

    /**
     * Updates the indicator position to stay aligned with the currently selected button.
     * Called during scrolling to keep the indicator synced with the button.
     */
    private void updateIndicatorPosition() {
        if (projectIndicator == null || currentSelectedButton == null) {
            return;
        }

        // Calculate the center X position of the current selected button
        int[] buttonLocation = new int[2];
        currentSelectedButton.getLocationOnScreen(buttonLocation);

        int[] indicatorLocation = new int[2];
        projectIndicator.getLocationOnScreen(indicatorLocation);

        // Calculate the translation needed to center the indicator under the button
        float buttonCenterX = buttonLocation[0] + (currentSelectedButton.getWidth() / 2f);
        float indicatorCenterX = indicatorLocation[0] + (projectIndicator.getWidth() / 2f);
        float targetTranslationX = projectIndicator.getTranslationX() + (buttonCenterX - indicatorCenterX);

        // Update position immediately without animation
        projectIndicator.setTranslationX(targetTranslationX);
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
        // Store current days to show for habit data aggregation
        currentDaysToShow = daysToShow;

        // Get current day of year
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_YEAR);

        // Prepare data for the selected time range
        List<Float> graphData = new ArrayList<>();

        if (daysToShow == -1) {
            // Show all time data - get all unique days from aggregatedTimeData
            if (aggregatedTimeData.isEmpty()) {
                // No data available
                graphData.add(0f);
            } else {
                // Find the earliest and latest days in the data
                int minDay = Integer.MAX_VALUE;
                int maxDay = Integer.MIN_VALUE;

                for (int day : aggregatedTimeData.keySet()) {
                    if (day < minDay) minDay = day;
                    if (day > maxDay) maxDay = day;
                }

                // For simplicity, show from minDay to maxDay (assuming same year)
                // If data spans across years, this needs more complex handling
                for (int day = minDay; day <= maxDay; day++) {
                    Double hours = aggregatedTimeData.get(day);
                    graphData.add(hours != null ? hours.floatValue() : 0f);
                }

                // Update currentDaysToShow to the actual range
                currentDaysToShow = maxDay - minDay + 1;
            }
        } else {
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

        // Update habit time breakdown RecyclerView
        updateHabitTimeBreakdown();

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

    private void updateHabitTimeBreakdown() {
        // Load userStorageList
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_MULTI_PROCESS);
        String json = userStorage.getString("userStorageList", null);

        // Map to store habit name -> total minutes
        Map<String, Integer> habitMinutesMap = new HashMap<>();

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
            ArrayList<dataStorage> storageList = gson.fromJson(json, type);

            // Check if we're showing all time data
            if (currentDaysToShow == -1) {
                // Show all data - no date filtering
                for (dataStorage entry : storageList) {
                    String habitName = entry.getName();
                    int minutes = Math.round((float) (entry.getHours()));

                    if (habitMinutesMap.containsKey(habitName)) {
                        habitMinutesMap.put(habitName, habitMinutesMap.get(habitName) + minutes);
                    } else {
                        habitMinutesMap.put(habitName, minutes);
                    }
                }
            } else {
                // Filter by date range
                Calendar cal = Calendar.getInstance();
                int currentDay = cal.get(Calendar.DAY_OF_YEAR);
                int startDay = currentDay - currentDaysToShow + 1;

                for (dataStorage entry : storageList) {
                    int day = entry.getDate();

                    // Check if day is in range
                    boolean inRange = false;
                    if (startDay > 0) {
                        inRange = (day >= startDay && day <= currentDay);
                    } else {
                        // Handle year wrap-around
                        int adjustedStartDay = 365 + startDay;
                        inRange = (day >= adjustedStartDay || day <= currentDay);
                    }

                    if (inRange) {
                        String habitName = entry.getName();
                        int minutes = Math.round((float) (entry.getHours()));

                        if (habitMinutesMap.containsKey(habitName)) {
                            habitMinutesMap.put(habitName, habitMinutesMap.get(habitName) + minutes);
                        } else {
                            habitMinutesMap.put(habitName, minutes);
                        }
                    }
                }
            }
        }

        // Create HabitTimeData list with images from PersonalHabits
        List<HabitTimeData> habitTimeDataList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : habitMinutesMap.entrySet()) {
            String habitName = entry.getKey();
            int totalMinutes = entry.getValue();

            // Find the image name from dynamicHabitList
            String imageName = "ionlogo"; // Default image
            for (DynamicHabit habit : dynamicHabitList) {
                if (habit.getName3().equals(habitName)) {
                    imageName = habit.getImageName();
                    break;
                }
            }

            habitTimeDataList.add(new HabitTimeData(habitName, imageName, totalMinutes));
        }

        // Sort by total minutes in descending order (highest first)
        Collections.sort(habitTimeDataList, new Comparator<HabitTimeData>() {
            @Override
            public int compare(HabitTimeData h1, HabitTimeData h2) {
                return Integer.compare(h2.getTotalMinutes(), h1.getTotalMinutes());
            }
        });

        // Update adapter with new data
        habitTimeAdapter.updateData(habitTimeDataList);
    }

}



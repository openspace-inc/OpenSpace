package com.example.iaso.Home;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iaso.BottomNavigationHelper;
import com.example.iaso.HabitTask;
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
    LinearLayout habitDetailContainer;
    ImageView habitDetailImage;
    TextView habitDetailName;
    androidx.constraintlayout.widget.ConstraintLayout hubContainer;

    // Feature 1: Daily Progress Bar views
    View progressBarFillGreen;
    FrameLayout progressBarFrame;
    TextView minutesLeftNumber;

    // Feature 2: Timeline views
    ImageView timelineImage;
    ImageView timelineArrow;
    TextView timelineDaysNumber;
    TextView timelineDaysLabel;
    TextView timelineUpgradeMessage;

    // Feature 3: Milestone card views
    TextView milestoneCardName;
    TextView milestoneCardDaysLeft;

    // Feature 4: Task list views
    LinearLayout habitTaskListContainer;
    CardView addTaskButton;

    // Currently selected habit for toggle behavior
    DynamicHabit currentlySelectedHabit = null;

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

        // Setup habit detail views
        habitDetailContainer = findViewById(R.id.habitDetailContainer);
        habitDetailImage = findViewById(R.id.habitDetailImage);
        habitDetailName = findViewById(R.id.habitDetailName);
        hubContainer = findViewById(R.id.Hub);

        // Feature 1: Progress bar views
        progressBarFillGreen = findViewById(R.id.progressBarFillGreen);
        progressBarFrame = findViewById(R.id.progressBarFrame);
        minutesLeftNumber = findViewById(R.id.minutesLeftNumber);

        // Feature 2: Timeline views
        timelineImage = findViewById(R.id.timelineImage);
        timelineArrow = findViewById(R.id.timelineArrow);
        timelineDaysNumber = findViewById(R.id.timelineDaysNumber);
        timelineDaysLabel = findViewById(R.id.timelineDaysLabel);
        timelineUpgradeMessage = findViewById(R.id.timelineUpgradeMessage);

        // Feature 3: Milestone card views
        milestoneCardName = findViewById(R.id.milestoneCardName);
        milestoneCardDaysLeft = findViewById(R.id.milestoneCardDaysLeft);

        // Feature 4: Task list views
        habitTaskListContainer = findViewById(R.id.habitTaskListContainer);
        addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> {
            if (currentlySelectedHabit != null) {
                showAddTaskDialog(currentlySelectedHabit.getName3());
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup timer resources
        BottomNavigationHelper.cleanup();
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
            
            // Set long-press listener to start focus timer
            button.setOnLongClickListener(v -> {
                // Start focus timer for this habit
                BottomNavigationHelper.startFocusTimer(
                    MainActivity.this,
                    habit.getName3(),
                    habit.getImageName(),
                    habit.getTime()
                );
                Toast.makeText(MainActivity.this, "Focus timer started for " + habit.getName3(), Toast.LENGTH_SHORT).show();
                return true; // Consume the long click
            });

            frame.addView(button);
            projectContainer.addView(frame);
            index++;
        }
    }

    // ==================== HABIT DETAIL METHODS ====================

    /**
     * Shows or toggles the habit detail expansion panel.
     * If the same habit is tapped again, the panel is hidden and the main view restored.
     * If a different habit is tapped, the panel switches to that habit's data.
     *
     * @param habit The DynamicHabit to display details for
     */
    private void showHabitDetail(DynamicHabit habit) {
        // If same habit tapped again, just show a toast
        if (currentlySelectedHabit != null
                && currentlySelectedHabit.getName3().equals(habit.getName3())
                && habitDetailContainer != null
                && habitDetailContainer.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "You are already on this project!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentlySelectedHabit = habit;

        // Hide Hub and RecyclerView
        if (hubContainer != null) {
            hubContainer.setVisibility(View.GONE);
        }
        if (habitTimeRecyclerView != null) {
            habitTimeRecyclerView.setVisibility(View.GONE);
        }

        // Show habit detail container with fade-in
        if (habitDetailContainer != null) {
            habitDetailContainer.setAlpha(0f);
            habitDetailContainer.setVisibility(View.VISIBLE);
            habitDetailContainer.animate().alpha(1f).setDuration(250).start();
        }

        // Set habit header (image + ticker symbol)
        if (habitDetailName != null) {
            habitDetailName.setText(habit.getStockSymbol());
        }
        if (habitDetailImage != null) {
            int imageRes = getResources().getIdentifier(habit.getImageName(), "drawable", getPackageName());
            Glide.with(this).load(imageRes).circleCrop().into(habitDetailImage);
        }

        // Show the fixed "Add Data" button
        if (addTaskButton != null) {
            addTaskButton.setVisibility(View.VISIBLE);
        }

        // Populate all 4 features
        updateProgressBar(habit);
        updateTimeline(habit);
        updateCurrentMilestone(habit);
        updateTaskList(habit);
    }

    /**
     * Shows the main view (Hub and RecyclerView) and hides habit detail view.
     */
    private void showMainView() {
        currentlySelectedHabit = null;

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

        // Hide the fixed "Add Data" button
        if (addTaskButton != null) {
            addTaskButton.setVisibility(View.GONE);
        }
    }

    // ==================== FEATURE 1: DAILY PROGRESS BAR ====================

    /**
     * Updates the vertical progress bar and minutes-left display for the selected habit.
     * Reads today's logged minutes from userStorage and compares against the daily target.
     *
     * @param habit The DynamicHabit whose progress to display
     */
    private void updateProgressBar(DynamicHabit habit) {
        int dailyTarget = habit.getTime();

        // Load today's logged minutes from userStorage SharedPreferences
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_MULTI_PROCESS);
        String json = userStorage.getString("userStorageList", null);

        double minutesWorkedToday = 0;
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
            ArrayList<dataStorage> storageList = gson.fromJson(json, type);

            Calendar cal = Calendar.getInstance();
            int todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR);

            if (storageList != null) {
                for (dataStorage entry : storageList) {
                    if (entry.getName().equals(habit.getName3()) && entry.getDate() == todayDayOfYear) {
                        minutesWorkedToday += entry.getHours();
                    }
                }
            }
        }

        // Calculate percentage and remaining minutes
        double percentage = dailyTarget > 0 ? Math.min(1.0, minutesWorkedToday / dailyTarget) : 0;
        int minutesLeft = Math.max(0, dailyTarget - (int) minutesWorkedToday);

        // Update the minutes-left number
        if (minutesLeftNumber != null) {
            minutesLeftNumber.setText(String.valueOf(minutesLeft));
        }

        // Update the green fill height as a percentage of the total bar height
        if (progressBarFillGreen != null && progressBarFrame != null) {
            progressBarFrame.post(() -> {
                int totalHeight = progressBarFrame.getHeight();
                int fillHeight = (int) (totalHeight * percentage);
                ViewGroup.LayoutParams params = progressBarFillGreen.getLayoutParams();
                params.height = fillHeight;
                progressBarFillGreen.setLayoutParams(params);
            });
        }
    }

    // ==================== FEATURE 2: TIMELINE ====================

    /**
     * Updates the timeline section for the selected habit.
     * Calculates progress based on days elapsed since creation vs total completion days.
     * Displays remaining time in years (or days if < 1 year).
     *
     * @param habit The DynamicHabit to display timeline for
     */
    private void updateTimeline(DynamicHabit habit) {
        // Load milestones to get total completion days
        MilestoneStorage milestoneStorage = new MilestoneStorage(this);
        List<Milestone> milestones = milestoneStorage.getMilestonesForHabit(habit.getName3());

        int totalCompletionDays = 0;
        for (Milestone milestone : milestones) {
            totalCompletionDays += milestone.getDays();
        }

        long creationDate = habit.getCreationDate();
        boolean hasValidData = creationDate > 0 && totalCompletionDays > 0;

        if (!hasValidData) {
            if (timelineUpgradeMessage != null) {
                timelineUpgradeMessage.setVisibility(View.VISIBLE);
            }
            if (timelineDaysNumber != null) {
                timelineDaysNumber.setText("0");
            }
            if (timelineDaysLabel != null) {
                timelineDaysLabel.setText("Yrs left");
            }
            if (timelineArrow != null) {
                timelineArrow.post(() -> animateTimelineArrow(0f));
            }
            return;
        }

        if (timelineUpgradeMessage != null) {
            timelineUpgradeMessage.setVisibility(View.GONE);
        }

        // Calculate days elapsed and remaining
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - creationDate;
        int daysElapsed = (int) (elapsedMillis / (1000L * 60 * 60 * 24));
        int daysRemaining = Math.max(0, totalCompletionDays - daysElapsed);

        // Progress percentage for the arrow
        float calculatedPercentage = ((float) daysElapsed / (float) totalCompletionDays) * 100f;
        final float progressPercentage = Math.min(calculatedPercentage, 100f);

        // Display remaining time as years (with 1 decimal) or days
        if (timelineDaysNumber != null && timelineDaysLabel != null) {
            if (daysRemaining >= 365) {
                double yearsLeft = daysRemaining / 365.0;
                timelineDaysNumber.setText(String.format("%.1f", yearsLeft));
                timelineDaysLabel.setText("Yrs left");
            } else {
                timelineDaysNumber.setText(String.valueOf(daysRemaining));
                timelineDaysLabel.setText("days left");
            }
        }

        // Animate the arrow
        if (timelineArrow != null && timelineImage != null) {
            timelineArrow.post(() -> animateTimelineArrow(progressPercentage));
        }
    }

    /**
     * Animates the timeline arrow to its target position based on progress percentage.
     *
     * @param progressPercentage The progress percentage (0-100)
     */
    private void animateTimelineArrow(float progressPercentage) {
        if (timelineArrow == null || timelineImage == null) {
            return;
        }

        int timelineWidth = timelineImage.getWidth();
        if (timelineWidth == 0) {
            timelineArrow.post(() -> animateTimelineArrow(progressPercentage));
            return;
        }

        int arrowWidth = timelineArrow.getWidth();
        float maxTranslation = timelineWidth - arrowWidth;
        float targetTranslationX = (progressPercentage / 100f) * maxTranslation;

        timelineArrow.setTranslationX(0f);

        ObjectAnimator animator = ObjectAnimator.ofFloat(timelineArrow, "translationX", 0f, targetTranslationX);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.start();
    }

    // ==================== FEATURE 3: CURRENT MILESTONE CARD ====================

    /**
     * Updates the milestone card with the first (current) milestone for the habit.
     * Shows the milestone name and remaining time. Displays fallback if no milestones exist.
     *
     * @param habit The DynamicHabit whose current milestone to display
     */
    private void updateCurrentMilestone(DynamicHabit habit) {
        MilestoneStorage milestoneStorage = new MilestoneStorage(this);
        List<Milestone> milestones = milestoneStorage.getMilestonesForHabit(habit.getName3());

        if (milestones == null || milestones.isEmpty()) {
            if (milestoneCardName != null) {
                milestoneCardName.setText("No milestones set");
            }
            if (milestoneCardDaysLeft != null) {
                milestoneCardDaysLeft.setText("");
            }
            return;
        }

        Milestone current = milestones.get(0);
        if (milestoneCardName != null) {
            milestoneCardName.setText(current.getName());
        }
        if (milestoneCardDaysLeft != null) {
            String timeStr = current.getTime();
            if (timeStr != null && !timeStr.isEmpty()) {
                milestoneCardDaysLeft.setText(timeStr + " left");
            } else {
                milestoneCardDaysLeft.setText("");
            }
        }
    }

    // ==================== FEATURE 4: DYNAMIC TO-DO LIST ====================

    /**
     * Loads and displays the task list for the selected habit.
     * Clears previous task rows and repopulates from SharedPreferences.
     *
     * @param habit The DynamicHabit whose tasks to display
     */
    private void updateTaskList(DynamicHabit habit) {
        if (habitTaskListContainer == null) return;

        habitTaskListContainer.removeAllViews();

        String habitName = habit.getName3();
        ArrayList<HabitTask> tasks = loadHabitTasks(habitName);

        if (tasks.isEmpty()) {
            // Show empty state
            TextView emptyText = new TextView(this);
            emptyText.setText("No tasks yet");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            emptyText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            habitTaskListContainer.addView(emptyText);
            return;
        }

        for (int i = 0; i < tasks.size(); i++) {
            HabitTask task = tasks.get(i);
            View row = createTaskRow(task, i, habitName);
            habitTaskListContainer.addView(row);
        }
    }

    /**
     * Creates a single task row View with icon and text.
     * Completed tasks show a green checkmark; incomplete tasks show os_circle and
     * support tap-to-complete with an animated removal after 5 seconds.
     *
     * @param task      The HabitTask to render
     * @param index     Position in the task list
     * @param habitName The habit name used as storage key
     * @return The constructed row View
     */
    private View createTaskRow(HabitTask task, int index, String habitName) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        icon.setLayoutParams(iconParams);

        TextView text = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMarginStart(dpToPx(12));
        text.setLayoutParams(textParams);
        text.setText(task.getTaskText());
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        text.setTextColor(Color.BLACK);

        if (task.isCompleted()) {
            icon.setImageResource(R.drawable.checkmarkv3);
        } else {
            icon.setImageResource(R.drawable.os_circle);
            icon.setOnClickListener(v -> {
                icon.setImageResource(R.drawable.checkmarkv3);
                task.setCompleted(true);
                // Reload, update the matching task, and save back
                ArrayList<HabitTask> allTasks = loadHabitTasks(habitName);
                for (HabitTask t : allTasks) {
                    if (t.getCreatedAt() == task.getCreatedAt()
                            && t.getTaskText().equals(task.getTaskText())) {
                        t.setCompleted(true);
                        break;
                    }
                }
                saveHabitTasks(habitName, allTasks);

                // Remove after 5 seconds with animation
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    row.animate()
                            .alpha(0f)
                            .translationX(-row.getWidth())
                            .setDuration(300)
                            .withEndAction(() -> {
                                if (row.getParent() != null) {
                                    ((ViewGroup) row.getParent()).removeView(row);
                                }
                                removeTask(habitName, task);
                            })
                            .start();
                }, 5000);
            });
        }

        row.addView(icon);
        row.addView(text);
        return row;
    }

    /**
     * Shows an AlertDialog to add a new task for the given habit.
     *
     * @param habitName The habit name to associate the new task with
     */
    private void showAddTaskDialog(String habitName) {
        EditText input = new EditText(this);
        input.setHint("Enter task description");
        input.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String taskText = input.getText().toString().trim();
                    if (!taskText.isEmpty()) {
                        ArrayList<HabitTask> tasks = loadHabitTasks(habitName);
                        tasks.add(new HabitTask(taskText));
                        saveHabitTasks(habitName, tasks);
                        // Refresh the task list
                        if (currentlySelectedHabit != null) {
                            updateTaskList(currentlySelectedHabit);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Loads the task list for a habit from SharedPreferences.
     *
     * @param habitName The habit name key
     * @return ArrayList of HabitTask objects (never null)
     */
    private ArrayList<HabitTask> loadHabitTasks(String habitName) {
        SharedPreferences prefs = getSharedPreferences("HabitTasks", Context.MODE_PRIVATE);
        String json = prefs.getString("tasks_" + habitName, null);
        if (json == null) return new ArrayList<>();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HabitTask>>(){}.getType();
        ArrayList<HabitTask> tasks = gson.fromJson(json, type);
        return tasks != null ? tasks : new ArrayList<>();
    }

    /**
     * Saves the given task list for a habit to SharedPreferences.
     *
     * @param habitName The habit name key
     * @param tasks     The task list to persist
     */
    private void saveHabitTasks(String habitName, ArrayList<HabitTask> tasks) {
        SharedPreferences prefs = getSharedPreferences("HabitTasks", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        prefs.edit().putString("tasks_" + habitName, json).apply();
    }

    /**
     * Removes a specific task from the persisted list and refreshes the UI if needed.
     *
     * @param habitName The habit name key
     * @param task      The HabitTask to remove
     */
    private void removeTask(String habitName, HabitTask task) {
        ArrayList<HabitTask> tasks = loadHabitTasks(habitName);
        tasks.removeIf(t -> t.getCreatedAt() == task.getCreatedAt()
                && t.getTaskText().equals(task.getTaskText()));
        saveHabitTasks(habitName, tasks);
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



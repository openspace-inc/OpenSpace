package com.example.iaso;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.iaso.FocusTimer.FocusTimerManager;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.PersonalPage.dataStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * BottomNavigationHelper
 *
 * A helper class to manage the reusable bottom navigation bar across different activities.
 * This class handles:
 * - Setting up click listeners for navigation buttons
 * - Managing button states (active/inactive with different drawables)
 * - Navigating between activities
 * - Managing focus timer UI state and updates
 *
 * Navigation mapping:
 * - navButtonWorkhorse (activity icon) -> Toast "Coming soon"
 * - navButtonHub (hub icon) -> MainActivity
 * - navButtonAnalytics (block icon) -> PersonalPage
 *
 * Focus Timer:
 * - Shows timer UI above navigation buttons when active
 * - Updates live every second
 * - Persists state across app restarts
 * - Tap minutes to pause/resume, tap image to stop
 *
 * Usage:
 * In your activity's onCreate, call:
 * BottomNavigationHelper.setupBottomNavigation(this, R.id.bottom_nav_include, CurrentActivity.class);
 */
public class BottomNavigationHelper {
    
    // Focus timer UI update interval (1 second)
    private static final long TIMER_UPDATE_INTERVAL_MS = 1000;
    
    // Handler for timer updates
    private static Handler timerUpdateHandler;
    private static Runnable timerUpdateRunnable;
    private static Activity currentActivity;
    private static FocusTimerManager timerManager;

    /**
     * Enum representing the different navigation destinations
     */
    public enum NavigationItem {
        ACTIVITY,
        HUB,
        PERSONALPAGE
    }

    /**
     * Sets up the bottom navigation bar for an activity
     *
     * @param activity The current activity
     * @param bottomNavViewId The resource ID of the included bottom navigation layout
     * @param currentActivityClass The class of the current activity (to highlight the active button)
     */
    public static void setupBottomNavigation(Activity activity, int bottomNavViewId, Class<?> currentActivityClass) {
        currentActivity = activity;
        
        View bottomNavView = activity.findViewById(bottomNavViewId);
        if (bottomNavView == null) {
            return;
        }

        ImageButton navButtonActivity = bottomNavView.findViewById(R.id.navButtonWorkhorse);
        ImageButton navButtonHub = bottomNavView.findViewById(R.id.navButtonHub);
        ImageButton navButtonPersonalPage = bottomNavView.findViewById(R.id.navButtonAnalytics);

        // Determine which button should be highlighted as active
        NavigationItem activeItem = getActiveNavigationItem(currentActivityClass);
        updateButtonStates(navButtonActivity, navButtonHub, navButtonPersonalPage, activeItem);

        // Set up click listeners
        if (navButtonActivity != null) {
            navButtonActivity.setOnClickListener(v -> {
                // Show "Coming soon" toast - not linked to any activity yet
                Toast.makeText(activity, "Coming soon! Stay tuned.", Toast.LENGTH_SHORT).show();
            });
        }

        if (navButtonHub != null) {
            navButtonHub.setOnClickListener(v -> {
                if (currentActivityClass != MainActivity.class) {
                    updateButtonStates(navButtonActivity, navButtonHub, navButtonPersonalPage, NavigationItem.HUB);
                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navButtonPersonalPage != null) {
            navButtonPersonalPage.setOnClickListener(v -> {
                if (currentActivityClass != PersonalPage.class) {
                    updateButtonStates(navButtonActivity, navButtonHub, navButtonPersonalPage, NavigationItem.PERSONALPAGE);
                    Intent intent = new Intent(activity, PersonalPage.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        
        // Initialize timer manager
        timerManager = new FocusTimerManager(activity);
        
        // Setup focus timer UI
        setupFocusTimerUI(activity, bottomNavView);
    }
    
    /**
     * Sets up the focus timer UI and initializes it based on current state
     * @param activity Current activity
     * @param bottomNavView Bottom navigation view
     */
    private static void setupFocusTimerUI(Activity activity, View bottomNavView) {
        // Find focus timer views
        ConstraintLayout focusTimerContainer = bottomNavView.findViewById(R.id.focusTimerContainer);
        ImageView habitImage = bottomNavView.findViewById(R.id.focusTimerHabitImage);
        TickerView minutesText = bottomNavView.findViewById(R.id.focusTimerMinutesText);
        View progressBarForeground = bottomNavView.findViewById(R.id.progressBarForeground);
        CardView bottomNavCard = bottomNavView.findViewById(R.id.bottomNavCard);
        
        if (focusTimerContainer == null || habitImage == null || minutesText == null) {
            return;
        }
        
        // Setup ticker view
        minutesText.setCharacterLists(TickerUtils.provideNumberList());
        Typeface neuehaas45 = activity.getResources().getFont(R.font.neuehaas45);
        minutesText.setTypeface(neuehaas45);
        
        // Check if timer is active
        if (timerManager.isTimerActive()) {
            // Show timer UI with animation
            showTimerUI(activity, bottomNavView, false);
            
            // Load habit image
            String imageName = timerManager.getHabitImageName();
            int imageRes = activity.getResources().getIdentifier(imageName, "drawable", activity.getPackageName());
            Glide.with(activity).load(imageRes).circleCrop().into(habitImage);
            
            // Start updating timer
            startTimerUpdates(bottomNavView);
        } else {
            // Hide timer UI
            focusTimerContainer.setVisibility(View.GONE);
        }
        
        // Setup click listeners
        // Tap image to stop timer
        habitImage.setOnClickListener(v -> {
            if (timerManager.isTimerActive()) {
                stopFocusTimer(activity, bottomNavView);
            }
        });
        
        // Tap minutes to pause/resume
        minutesText.setOnClickListener(v -> {
            if (timerManager.isTimerActive()) {
                if (timerManager.isTimerPaused()) {
                    timerManager.resumeTimer();
                    // Resume updates
                    startTimerUpdates(bottomNavView);
                } else {
                    timerManager.pauseTimer();
                    // Stop updates (timer is paused)
                    stopTimerUpdates();
                }
            }
        });
    }
    
    /**
     * Starts a focus timer for a habit
     * @param activity Current activity
     * @param habitName Name of the habit
     * @param habitImageName Image name for the habit
     * @param dailyGoalMinutes Daily time commitment goal
     */
    public static void startFocusTimer(Activity activity, String habitName, String habitImageName, int dailyGoalMinutes) {
        if (timerManager == null) {
            timerManager = new FocusTimerManager(activity);
        }
        
        // Start the timer
        timerManager.startTimer(habitName, habitImageName, dailyGoalMinutes);
        
        // Find bottom nav view
        View bottomNavView = activity.findViewById(R.id.bottom_nav_include);
        if (bottomNavView == null) {
            return;
        }
        
        // Show timer UI with animation
        showTimerUI(activity, bottomNavView, true);
        
        // Load habit image
        ImageView habitImage = bottomNavView.findViewById(R.id.focusTimerHabitImage);
        if (habitImage != null) {
            int imageRes = activity.getResources().getIdentifier(habitImageName, "drawable", activity.getPackageName());
            Glide.with(activity).load(imageRes).circleCrop().into(habitImage);
        }
        
        // Start updating timer
        startTimerUpdates(bottomNavView);
    }
    
    /**
     * Shows the timer UI with optional animation
     * Dynamically calculates the height needed based on timer container size
     * @param activity Current activity
     * @param bottomNavView Bottom navigation view
     * @param animate Whether to animate the appearance
     */
    private static void showTimerUI(Activity activity, View bottomNavView, boolean animate) {
        ConstraintLayout focusTimerContainer = bottomNavView.findViewById(R.id.focusTimerContainer);
        CardView bottomNavCard = bottomNavView.findViewById(R.id.bottomNavCard);
        
        if (focusTimerContainer == null || bottomNavCard == null) {
            return;
        }
        
        // Calculate the height needed
        // Base navigation height: 90dp (includes buttons at bottom)
        // When timer is active, we need additional space at the top:
        // - Container padding top: 12dp
        // - Timer container padding: 8dp top + 8dp bottom = 16dp
        // - Timer elements height: ~24dp (height of image/content)
        // - Container padding bottom: 4dp
        // Total addition: 12 + 16 + 24 + 4 = 56dp
        final int baseHeight = dpToPx(activity, 90);
        final int timerAddition = dpToPx(activity, 70);
        final int targetHeight = baseHeight + timerAddition;
        
        if (animate) {
            // Animate height increase
            int currentHeight = bottomNavCard.getLayoutParams().height;
            
            ValueAnimator heightAnim = ValueAnimator.ofInt(currentHeight, targetHeight);
            heightAnim.setDuration(300);
            heightAnim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = bottomNavCard.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                bottomNavCard.setLayoutParams(params);
            });
            heightAnim.start();
            
            // Fade in timer container after height animation starts
            focusTimerContainer.setVisibility(View.VISIBLE);
            focusTimerContainer.setAlpha(0f);
            focusTimerContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(150)
                .start();
        } else {
            // Show immediately without animation
            ViewGroup.LayoutParams params = bottomNavCard.getLayoutParams();
            params.height = targetHeight;
            bottomNavCard.setLayoutParams(params);
            
            focusTimerContainer.setVisibility(View.VISIBLE);
            focusTimerContainer.setAlpha(1f);
        }
    }
    
    /**
     * Hides the timer UI with animation
     * Returns the card height to its base size
     * @param activity Current activity
     * @param bottomNavView Bottom navigation view
     */
    private static void hideTimerUI(Activity activity, View bottomNavView) {
        ConstraintLayout focusTimerContainer = bottomNavView.findViewById(R.id.focusTimerContainer);
        CardView bottomNavCard = bottomNavView.findViewById(R.id.bottomNavCard);
        
        if (focusTimerContainer == null || bottomNavCard == null) {
            return;
        }
        
        // Fade out timer container
        focusTimerContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    focusTimerContainer.setVisibility(View.GONE);
                }
            })
            .start();
        
        // Animate height decrease back to base height
        int currentHeight = bottomNavCard.getLayoutParams().height;
        int baseHeight = dpToPx(activity, 90);
        
        ValueAnimator heightAnim = ValueAnimator.ofInt(currentHeight, baseHeight);
        heightAnim.setDuration(300);
        heightAnim.setStartDelay(150);
        heightAnim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = bottomNavCard.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            bottomNavCard.setLayoutParams(params);
        });
        heightAnim.start();
    }
    
    /**
     * Starts periodic updates of the timer UI
     * @param bottomNavView Bottom navigation view
     */
    private static void startTimerUpdates(View bottomNavView) {
        // Stop existing updates if any
        stopTimerUpdates();
        
        // Create handler if needed
        if (timerUpdateHandler == null) {
            timerUpdateHandler = new Handler(Looper.getMainLooper());
        }
        
        // Create update runnable
        timerUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimerUI(bottomNavView);
                
                // Schedule next update
                if (timerManager != null && timerManager.isTimerActive() && !timerManager.isTimerPaused()) {
                    timerUpdateHandler.postDelayed(this, TIMER_UPDATE_INTERVAL_MS);
                }
            }
        };
        
        // Start updates
        timerUpdateHandler.post(timerUpdateRunnable);
    }
    
    /**
     * Stops periodic updates of the timer UI
     */
    private static void stopTimerUpdates() {
        if (timerUpdateHandler != null && timerUpdateRunnable != null) {
            timerUpdateHandler.removeCallbacks(timerUpdateRunnable);
        }
    }
    
    /**
     * Updates the timer UI with current values
     * @param bottomNavView Bottom navigation view
     */
    private static void updateTimerUI(View bottomNavView) {
        if (timerManager == null || !timerManager.isTimerActive()) {
            return;
        }
        
        TickerView minutesText = bottomNavView.findViewById(R.id.focusTimerMinutesText);
        View progressBarForeground = bottomNavView.findViewById(R.id.progressBarForeground);
        
        if (minutesText == null || progressBarForeground == null) {
            return;
        }
        
        // Update minutes counter
        int elapsedMinutes = timerManager.getElapsedMinutes();
        minutesText.setText(String.valueOf(elapsedMinutes));
        
        // Update progress bar
        float progressRatio = timerManager.getProgressRatio();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) progressBarForeground.getLayoutParams();
        params.matchConstraintPercentWidth = progressRatio;
        progressBarForeground.setLayoutParams(params);
    }
    
    /**
     * Stops the focus timer and saves data
     * @param activity Current activity
     * @param bottomNavView Bottom navigation view
     */
    private static void stopFocusTimer(Activity activity, View bottomNavView) {
        if (timerManager == null || !timerManager.isTimerActive()) {
            return;
        }
        
        // Get elapsed time before stopping
        int elapsedMinutes = timerManager.getElapsedMinutes();
        String habitName = timerManager.getHabitName();
        
        // Stop the timer
        timerManager.stopTimer();
        
        // Stop UI updates
        stopTimerUpdates();
        
        // Hide timer UI
        hideTimerUI(activity, bottomNavView);
        
        // Save data to userStorage
        if (elapsedMinutes > 0) {
            saveTimerData(activity, habitName, elapsedMinutes);
        }
    }
    
    /**
     * Saves timer data to userStorage SharedPreferences
     * @param context Application context
     * @param habitName Name of the habit
     * @param minutes Minutes worked
     */
    private static void saveTimerData(Context context, String habitName, int minutes) {
        SharedPreferences dataStorage = context.getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = dataStorage.edit();
        
        String json = dataStorage.getString("userStorageList", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        ArrayList<dataStorage> dataStorageList = gson.fromJson(json, type);
        
        // Initialize list if null
        if (dataStorageList == null) {
            dataStorageList = new ArrayList<>();
        }
        
        // Create new data entry
        dataStorage newData = new dataStorage(habitName, "Personal", minutes);
        dataStorageList.add(newData);
        
        // Save back to SharedPreferences
        String updatedJson = gson.toJson(dataStorageList);
        dataEditor.putString("userStorageList", updatedJson);
        dataEditor.apply();
        
        Toast.makeText(context, "Logged " + minutes + " minutes for " + habitName, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Cleans up timer resources when activity is destroyed
     * Call this from activity's onDestroy
     */
    public static void cleanup() {
        stopTimerUpdates();
        currentActivity = null;
    }

    /**
     * Determines which navigation item should be active based on the current activity
     *
     * @param currentActivityClass The class of the current activity
     * @return The active NavigationItem
     */
    private static NavigationItem getActiveNavigationItem(Class<?> currentActivityClass) {
        if (currentActivityClass == PersonalPage.class) {
            return NavigationItem.PERSONALPAGE;
        } else if (currentActivityClass == Analytics.class) {
            // Analytics is a sub-view of PersonalPage, so PersonalPage button should be active
            return NavigationItem.PERSONALPAGE;
        } else if (currentActivityClass == MainActivity.class) {
            return NavigationItem.HUB;
        }
        return NavigationItem.HUB; // Default to hub
    }

    /**
     * Updates the button states (images) based on which item is active
     * Active buttons show white icons, inactive buttons show black icons
     *
     * @param navButtonActivity The activity navigation button (coming soon)
     * @param navButtonHub The hub navigation button
     * @param navButtonPersonalPage The personal page navigation button
     * @param activeItem The currently active navigation item
     */
    private static void updateButtonStates(ImageButton navButtonActivity,
                                          ImageButton navButtonHub,
                                          ImageButton navButtonPersonalPage,
                                          NavigationItem activeItem) {
        // Activity button is never active (always black) since it's "coming soon"
        if (navButtonActivity != null) {
            navButtonActivity.setImageResource(R.drawable.activity_black_menu);
        }

        if (navButtonHub != null) {
            if (activeItem == NavigationItem.HUB) {
                navButtonHub.setImageResource(R.drawable.hub_white_menu);
            } else {
                navButtonHub.setImageResource(R.drawable.hub_black_menu);
            }
        }

        if (navButtonPersonalPage != null) {
            if (activeItem == NavigationItem.PERSONALPAGE) {
                navButtonPersonalPage.setImageResource(R.drawable.block_white_menu);
            } else {
                navButtonPersonalPage.setImageResource(R.drawable.block_black_menu);
            }
        }
    }
    
    /**
     * Converts dp to pixels
     * @param context Application context
     * @param dp Value in dp
     * @return Value in pixels
     */
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

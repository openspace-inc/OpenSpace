package com.example.iaso;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.example.iaso.Home.MainActivity;

/**
 * BottomNavigationHelper
 *
 * A helper class to manage the reusable bottom navigation bar across different activities.
 * This class handles:
 * - Setting up click listeners for navigation buttons
 * - Managing button states (active/inactive with different drawables)
 * - Navigating between activities
 *
 * Usage:
 * In your activity's onCreate, call:
 * BottomNavigationHelper.setupBottomNavigation(this, R.id.bottom_nav_include, CurrentActivity.class);
 */
public class BottomNavigationHelper {

    /**
     * Enum representing the different navigation destinations
     */
    public enum NavigationItem {
        WORKHORSE,
        HUB,
        ANALYTICS
    }

    /**
     * Sets up the bottom navigation bar for an activity
     *
     * @param activity The current activity
     * @param bottomNavViewId The resource ID of the included bottom navigation layout
     * @param currentActivityClass The class of the current activity (to highlight the active button)
     */
    public static void setupBottomNavigation(Activity activity, int bottomNavViewId, Class<?> currentActivityClass) {
        View bottomNavView = activity.findViewById(bottomNavViewId);
        if (bottomNavView == null) {
            return;
        }

        ImageButton navButtonWorkhorse = bottomNavView.findViewById(R.id.navButtonWorkhorse);
        ImageButton navButtonHub = bottomNavView.findViewById(R.id.navButtonHub);
        ImageButton navButtonAnalytics = bottomNavView.findViewById(R.id.navButtonAnalytics);

        // Determine which button should be highlighted as active
        NavigationItem activeItem = getActiveNavigationItem(currentActivityClass);
        updateButtonStates(navButtonWorkhorse, navButtonHub, navButtonAnalytics, activeItem);

        // Set up click listeners
        if (navButtonWorkhorse != null) {
            navButtonWorkhorse.setOnClickListener(v -> {
                if (currentActivityClass != workhorse.class) {
                    updateButtonStates(navButtonWorkhorse, navButtonHub, navButtonAnalytics, NavigationItem.WORKHORSE);
                    Intent intent = new Intent(activity, workhorse.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navButtonHub != null) {
            navButtonHub.setOnClickListener(v -> {
                if (currentActivityClass != MainActivity.class) {
                    updateButtonStates(navButtonWorkhorse, navButtonHub, navButtonAnalytics, NavigationItem.HUB);
                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (navButtonAnalytics != null) {
            navButtonAnalytics.setOnClickListener(v -> {
                if (currentActivityClass != Analytics.class) {
                    updateButtonStates(navButtonWorkhorse, navButtonHub, navButtonAnalytics, NavigationItem.ANALYTICS);
                    Intent intent = new Intent(activity, Analytics.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    /**
     * Determines which navigation item should be active based on the current activity
     *
     * @param currentActivityClass The class of the current activity
     * @return The active NavigationItem
     */
    private static NavigationItem getActiveNavigationItem(Class<?> currentActivityClass) {
        if (currentActivityClass == workhorse.class) {
            return NavigationItem.WORKHORSE;
        } else if (currentActivityClass == Analytics.class) {
            return NavigationItem.ANALYTICS;
        } else if (currentActivityClass == MainActivity.class) {
            return NavigationItem.HUB;
        }
        return NavigationItem.HUB; // Default to hub
    }

    /**
     * Updates the button states (images) based on which item is active
     * Active buttons show white icons, inactive buttons show black icons
     *
     * @param navButtonWorkhorse The workhorse navigation button
     * @param navButtonHub The hub navigation button
     * @param navButtonAnalytics The analytics navigation button
     * @param activeItem The currently active navigation item
     */
    private static void updateButtonStates(ImageButton navButtonWorkhorse,
                                          ImageButton navButtonHub,
                                          ImageButton navButtonAnalytics,
                                          NavigationItem activeItem) {
        if (navButtonWorkhorse != null) {
            if (activeItem == NavigationItem.WORKHORSE) {
                navButtonWorkhorse.setImageResource(R.drawable.activity_white_menu);
            } else {
                navButtonWorkhorse.setImageResource(R.drawable.activity_black_menu);
            }
        }

        if (navButtonHub != null) {
            if (activeItem == NavigationItem.HUB) {
                navButtonHub.setImageResource(R.drawable.hub_white_menu);
            } else {
                navButtonHub.setImageResource(R.drawable.hub_black_menu);
            }
        }

        if (navButtonAnalytics != null) {
            if (activeItem == NavigationItem.ANALYTICS) {
                navButtonAnalytics.setImageResource(R.drawable.block_white_menu);
            } else {
                navButtonAnalytics.setImageResource(R.drawable.block_black_menu);
            }
        }
    }
}

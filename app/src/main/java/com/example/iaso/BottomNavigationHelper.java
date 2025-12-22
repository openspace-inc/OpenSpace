package com.example.iaso;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.iaso.Home.MainActivity;
import com.example.iaso.PersonalPage.PersonalPage;

/**
 * BottomNavigationHelper
 *
 * A helper class to manage the reusable bottom navigation bar across different activities.
 * This class handles:
 * - Setting up click listeners for navigation buttons
 * - Managing button states (active/inactive with different drawables)
 * - Navigating between activities
 *
 * Navigation mapping:
 * - navButtonWorkhorse (activity icon) -> Toast "Coming soon"
 * - navButtonHub (hub icon) -> MainActivity
 * - navButtonAnalytics (block icon) -> PersonalPage
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
}

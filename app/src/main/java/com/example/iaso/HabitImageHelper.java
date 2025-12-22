package com.example.iaso;

import com.example.iaso.PersonalPage.DynamicHabit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utility class for generating random habit images.
 * Shared between AddDynamicHabit and workhorse activities.
 */
public class HabitImageHelper {

    private static final String[] IMAGES = {"calmshades", "orb1", "orb2", "orb3", "orb4", "orb5", "orb6"};

    /**
     * Generates a random image name from the available images,
     * preferring images that are not already used by existing habits.
     *
     * @param existingHabits List of existing DynamicHabit objects to check for used images
     * @return A random image name from the available pool
     */
    public static String getRandomUnusedImage(ArrayList<DynamicHabit> existingHabits) {
        Set<String> used = new HashSet<>();
        if (existingHabits != null) {
            for (DynamicHabit habit : existingHabits) {
                used.add(habit.getImageName());
            }
        }

        List<String> available = new ArrayList<>();
        for (String img : IMAGES) {
            if (!used.contains(img)) {
                available.add(img);
            }
        }

        Random random = new Random();
        if (available.isEmpty()) {
            return IMAGES[random.nextInt(IMAGES.length)];
        }

        return available.get(random.nextInt(available.size()));
    }
}

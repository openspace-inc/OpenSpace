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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.example.iaso.Introduction.WelcomeActivity;
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.Profile;
import com.example.iaso.R;
import com.example.iaso.ToDoList.TaskLister;
import com.example.iaso.ContributionGraphView; // Add this import

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Back Button For Fragments
    ImageButton back;

    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<>(); // Changed to diamond operator
    LottieAnimationView dynamicLogo2;
    LinearLayout projectContainer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Open IntroActivity On First Run
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE) // Changed to primitive boolean
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
        profile.setOnClickListener(view -> { // Changed to lambda
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        });

        ImageButton imageButton2 = findViewById(R.id.imageButton2);

        imageButton2.setOnClickListener(v -> { // Changed to lambda
            Toast.makeText(getApplicationContext(), "Coming soon! Stay tuned.", Toast.LENGTH_SHORT).show();
        });

        //Create Habits

        //Set IntroText
        TextView introText = findViewById(R.id.generatedText);
        setIntroText(introText);

        //Set Dynamic Logo(old gif file)
        //ImageView dynamicLogoIaso = findViewById(R.id.dynamicLogo);
        //Glide.with(this).asGif().load(R.drawable.elastodynamics).into(dynamicLogoIaso);

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
        loadPersonalHabits();
        populateProjectRow();

        // Initialize the ContributionGraphView
        ContributionGraphView contributionGraph = findViewById(R.id.contributionGraph);
        // Sample data to display (replace with your actual data later)
        java.util.HashMap<String, Integer> sampleData = new java.util.HashMap<>();
        sampleData.put("2025-09-01", 1);  // Light gray
        sampleData.put("2025-09-08", 2);  // Medium gray
        sampleData.put("2025-09-15", 3);  // Darker gray
        sampleData.put("2025-09-22", 5);  // Black
        contributionGraph.updateData(sampleData);
    }

    private void loadPersonalHabits() {
        dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_PRIVATE); // Changed MODE_MULTI_PROCESS to MODE_PRIVATE
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

        int index = 0;
        for (DynamicHabit habit : dynamicHabitList) {
            // WARNING: Use of getIdentifier is discouraged. Consider refactoring to use R.drawable directly if possible.
            int imageRes = getResources().getIdentifier(habit.getImageName(), "drawable", getPackageName());

            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(125), dpToPx(125));
            if (index == 0) {
                params.setMargins(0, dpToPx(5), dpToPx(5), dpToPx(5));
            } else {
                params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            }
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
            index++;
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    //Create arrayList where habits are stored in shared preferences.
    // Method 'fillDynamicHabits()' is never used, commented out.

    //Send User To Health Activity
    // Method 'goToHealth(android.view.View)' is never used, commented out.

    // Method 'goToWelcomeTESTER(android.view.View)' is never used, commented out.

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
    // Method 'goToExercise(android.view.View)' is never used, commented out.

    //Send User To Store Activity
    public void goToStore(View v){
        Intent b = new Intent(this, Store.class);
        startActivity(b);
    }

    // Method 'goToProjects(android.view.View)' is never used, commented out.


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
        boolean firstRun = userData.getBoolean("firstRun", false); // Changed to primitive boolean
        String timingText; // Removed redundant initializer

        //Get only first name
        String firstName; // Removed redundant initializer
        int firstSpaceIndex = name.indexOf(" ");

        if (firstSpaceIndex != -1) {
            firstName= name.substring(0, firstSpaceIndex);
        } else {
            firstName = name;
        }
        //Get Time
        Date thisDate2 = new Date();
        SimpleDateFormat dateForm2 = new SimpleDateFormat("HH", Locale.getDefault()); // Added Locale.getDefault()
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
}

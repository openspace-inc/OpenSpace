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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.Profile;
import com.example.iaso.Projects;
import com.example.iaso.R;
import com.example.iaso.ToDoList.TaskLister;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Back Button For Fragments
    ImageButton back;

    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    LottieAnimationView dynamicLogo2;

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
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();

        //Create Animation of ImageButtons
        ImageButton toDoList = findViewById(R.id.todolistButton);
        ImageButton toDoList2 = findViewById(R.id.circle1); //FIRE
        ImageButton storeButton2 = findViewById(R.id.circle2); //FIRE
        ImageButton habitButton = findViewById(R.id.circle3); //FIRE
        ImageButton storeButton = findViewById(R.id.storeButton);
        TextView assistantText = findViewById(R.id.dynamicAssistantText);
        CardView dynamicAssistant = findViewById(R.id.cardView);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation fadeInAnimation3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation upwardsFade = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation.setStartOffset(300);
        fadeInAnimation2.setStartOffset(600);
        fadeInAnimation3.setStartOffset(900);

        toDoList.startAnimation(fadeInAnimation);
        toDoList2.startAnimation(fadeInAnimation); //FIRE
        storeButton2.startAnimation(fadeInAnimation2); //FIRE
        habitButton.startAnimation(fadeInAnimation3); //FIRE
        storeButton.startAnimation(fadeInAnimation3);
        assistantText.startAnimation(fadeInAnimation3);
        dynamicAssistant.startAnimation(upwardsFade);

        //Profile Page Setup
        ImageButton profile = findViewById(R.id.profileIcon);
        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
            }
        });

        ImageButton imageButton2 = findViewById(R.id.imageButton2);

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Coming soon! Stay tuned.", Toast.LENGTH_SHORT).show();
            }
        });

        //Create Habits

        //Set IntroText
        TextView introText = findViewById(R.id.generatedText);
        setIntroText(introText);

        //Set gif tester file
        ImageView gifImageView = findViewById(R.id.imageView4);
        Glide.with(this).asGif().load(R.drawable.iasoxgif4).into(gifImageView);

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

        //Run Dynamic Assistant
        runDynamicAssistant(gifImageView);

        //Send user to Personal Habit Fragment
        ImageButton habitBTN = findViewById(R.id.circle3);
        habitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, PersonalHabitFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack("name")
                        .commit();

                gifImageView.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                dynamicAssistant.setVisibility(View.GONE);
            }
        });

        fillDynamicHabits();
    }

    //Create arrayList where habits are stored in sharedpreferences.
    public void fillDynamicHabits() {
        dynamicHabits = getSharedPreferences("DynamicHabits", Context.MODE_PRIVATE);
        SharedPreferences.Editor dynamicHabitEditor = dynamicHabits.edit();

        DynamicHabit pullUpHabit = new DynamicHabit("Pull Ups", 0, "Fitness", "Strength Your UpperBack Through Pullups", 5, 15, R.drawable.calmshades, 0);
        DynamicHabit pullUpHabit2 = new DynamicHabit("Pull Ups2", 4, "Fitness", "Strength Your UpperBack Through Pullups23", 256, 15, R.drawable.calmshades, 0);
        dynamicHabitList.add(pullUpHabit);
        dynamicHabitList.add(pullUpHabit2);

        Gson gson = new Gson();

        String json = gson.toJson(dynamicHabitList);
        dynamicHabitEditor.putString("dynamicHabitList", json);
        dynamicHabitEditor.apply();
    }

    //Create Dynamic Assistant
    private void runDynamicAssistant(ImageView IASOX) {
        TextView title = findViewById(R.id.titleInfoCardDescription);
        TextView description = findViewById(R.id.InfoCardDescription);
        ImageView gifIASO = IASOX;
        Button reset = findViewById(R.id.button5);

        reset.setVisibility(View.GONE);

        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> descriptions = new ArrayList<>();
        String currentTitle;
        String currentDescription;

        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String name = userData.getString("name","");
        int points = userData.getInt("userPoints", 0);
        String stringPoints = Integer.toString(points);

        //Fill In
        titles.add("Your Current Worth, " + name);
        titles.add("Heads up " + name + "!");
        titles.add("Something Not Working Correctly?");
        titles.add("Hey " + name);
        titles.add("New Features Coming Soon");
        titles.add("Make Sure To Do Your Tasks");
        titles.add("Check Out The Store!");
        titles.add("Dynamic Assistant");
        titles.add("You're Doing Great, " + name);
        titles.add("Nice Work, " + name);
        titles.add("Use The Dynamic To-Do List!");
        titles.add(name + " ,");

        descriptions.add(stringPoints + " blocks");
        descriptions.add("IASO X (Premium Edition) Releases Early 2024. All BETA Members Recieve It Free. Thanks For Being With Us.");
        descriptions.add("All blocks and data will be CLEARED!");
        descriptions.add("Make Sure To Do Your Habits On The Health Page. Get Blocks As You Complete Them");
        descriptions.add("You're Currently On The BETA Model. New Features And Bug Fixes Coming Soon");
        descriptions.add("Get 10 Blocks Per Task. Try It Out!");
        descriptions.add("New Items Launching Soon!");
        descriptions.add("My Job Is To Help You Succeed. Check Back Here For Suggestions, Tips, And Motivation.");
        descriptions.add("Keep It Going! It Will Pay Off Greatly Someday");
        descriptions.add("Great Job Today.");
        descriptions.add("Make Sure To Use The Dynamic To-Do List. Press The Add Button To Add Tasks. Press To " +
                "Open Expanded View. Hold To Complete!");
        descriptions.add("Use The BMR Calculator In The Health Section. It Will Let You Know How Many Calories To " +
                "Eat Any Time Of Day. Stick To It, And You'll Be Healthy In No Time!");


        Random random = new Random();
        int randomNumber = random.nextInt(titles.size());

        currentTitle = titles.get(randomNumber);
        currentDescription = descriptions.get(randomNumber);

        //Print
        if(randomNumber == 0){
            title.setText(currentTitle);
            description.setText(currentDescription);
            gifIASO.setVisibility(View.GONE);
        }
        else if(randomNumber == 1){
            title.setText(currentTitle);
            description.setText(currentDescription);
            gifIASO.setVisibility(View.VISIBLE);
        }
        else if(randomNumber == 2){
            title.setText(currentTitle);
            description.setText(currentDescription);
            gifIASO.setVisibility(View.GONE);
            reset.setVisibility(View.VISIBLE);
        }
        else{
            title.setText(currentTitle);
            description.setText(currentDescription);
            gifIASO.setVisibility(View.GONE);
        }

    }

    //Send User To Health Activity
    public void goToHealth(View v){
        Intent b = new Intent(this, Health.class);
        startActivity(b);
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
    }


    //Send User To Exercise Activity
    public void goToExercise(View v){
        Intent b = new Intent(this, PersonalPage.class);
        startActivity(b);
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
        CardView dynamicAssistant = findViewById(R.id.cardView);
        dynamicAssistant.setVisibility(View.VISIBLE);
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

}



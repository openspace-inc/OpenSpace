package com.example.iaso.Health;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.iaso.Fitness.Exercise;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Health extends AppCompatActivity {
    public ArrayList<healthyHabits> list = new ArrayList<healthyHabits>();
    ConstraintLayout firstLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health);


        //Calculate BMR Data from sharedPrefs
        SharedPreferences userData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);

        String age = userData.getString("age", "");
        String gender = userData.getString("gender", "");
        String weight = userData.getString("weight", "");
        String height = userData.getString("height", "");
        String exercise = userData.getString("exercise", "");

        int bmr = calculateBMR(age, gender, weight, height, exercise); //Run BMR method
        int breakfast = (int)Math.round(bmr * 0.35);
        int lunch = (int)Math.round(bmr * 0.40);
        int dinner = (int)Math.round(bmr * 0.25);

        //Send BMR + Food Calculations To SharedPrefs
        SharedPreferences.Editor editor = userData.edit();
        editor.putInt("breakfastCal", breakfast);
        editor.putInt("lunchCal", lunch);
        editor.putInt("dinnerCal", dinner);
        editor.putInt("BMR", bmr);
        editor.apply();


        //Use time to change picture and text
        Date thisDate2 = new Date();
        SimpleDateFormat dateForm2 = new SimpleDateFormat("HH");
        String stringTime = dateForm2.format(thisDate2);
        int currentTime = Integer.parseInt(stringTime);

        TextView timingText = findViewById(R.id.mealSetter);
        ImageButton sunImage = (ImageButton)findViewById(R.id.sunImage);

        if((currentTime >= 5) && (currentTime < 11)){
            timingText.setText("Your Calories For Breakfast");
            bmr = breakfast;
            sunImage.setImageResource(R.drawable.sunrisesun);
        }
        else if ((currentTime >= 11) && (currentTime < 17)){
            timingText.setText("Your Calories For Lunch");
            bmr = lunch;
            sunImage.setImageResource(R.drawable.noonsun);
        }
        else if ((currentTime >= 17) && (currentTime <= 20)){
            timingText.setText("Your Calories For Dinner");
            bmr = dinner;
            sunImage.setImageResource(R.drawable.afternoonsun);
        }
        else{
            timingText.setText("Your Calories For Dinner");
            bmr = dinner;
            sunImage.setImageResource(R.drawable.nightsun);
        }

        //Use BMR to calculate and set calories
        String bmr1 = Integer.toString(bmr);
        TextView calorieCounter = findViewById(R.id.caloriescounter);
        calorieCounter.setText(bmr1 + " Cal");


        //Gradient Color
        TextPaint paint = calorieCounter.getPaint();
        float width = paint.measureText(bmr1);

        Shader textShader = new LinearGradient(0, 0, width, calorieCounter.getTextSize(),
                new int[]{
                        Color.parseColor("#de6262"),
                        Color.parseColor("#ffb88c"),
                }, null, Shader.TileMode.CLAMP);
        calorieCounter.getPaint().setShader(textShader);


        //Create ArrayList Items For HealthyHabits
        healthyHabits drinkWater = new healthyHabits("Drink Water", "E", 1, R.drawable.waterbottle, "Ex. 2 Cups of Water");
        healthyHabits eatApple = new healthyHabits("Eat Fruit", "M", 1, R.drawable.orange, "Ex. Eat An Orange");
        healthyHabits walk = new healthyHabits("Go For A Walk", "H", 1, R.drawable.walk, "Ex. 10 Min Sounds Good");
        healthyHabits yoga = new healthyHabits("Yoga", "M", 1, R.drawable.yoga, "Ex. Try For 10 Min At Least");
        healthyHabits sport = new healthyHabits("Go Play", "M", 1, R.drawable.sport, "Ex. Play A Sport For 10 Min");
        healthyHabits healthyEating = new healthyHabits("Eat Something Nice", "M", 1, R.drawable.cake, "Ex. How About Some Desert");

        list.add(drinkWater);
        list.add(eatApple);
        list.add(walk);
        list.add(yoga);
        list.add(sport);
        list.add(healthyEating);
        createHealthyHabits(list);


        //Turning congratulations layout on and off
        firstLayout = findViewById(R.id.congrats);
        hideView(firstLayout); //method to turn off congrats layout


        //Run NewDay Tester to determine whether to randomly change habits
        newDayTester(1);


        //Coming Soon Text
        TextView comingSoon = findViewById(R.id.comingSoonText);
        comingSoon.setText("Streaks, Personalized Health, And FAR More Coming Soon");

        Animation fadeInLiftAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        comingSoon.startAnimation(fadeInLiftAnimation);
    }

    //Methods To Show And Disable Congrats Constraint Layout
    public static void showView(View view){
        view.setVisibility(View.VISIBLE);
    }
    public static void hideView(View view){
        view.setVisibility(View.GONE);
    }


    public void transferToPersonal (View v){
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
    }
    public void transferToExercise (View v){
        Intent b = new Intent(this, Exercise.class);
        startActivity(b);
    }


    //Calculate BMR Method
    public int calculateBMR (String age, String gender, String weight, String height, String exercise){
        double bmr = 0.0;

        //Gender calculations
        if(gender.equalsIgnoreCase("M")){
            bmr = 66 + (6.23 * Double.parseDouble(weight)) + (12.7 * Double.parseDouble(height)) - (6.8 * Double.parseDouble(age));
        }
        else {
            bmr = 655 + (4.35 * Double.parseDouble(weight)) + (4.7 * Double.parseDouble(height)) - (4.7 * Double.parseDouble(age));
        }

        //Exercise calculations
        if (exercise.equals("1")){
            bmr = bmr * 1.2;
        }
        else if(exercise.equals("2")){
            bmr = bmr * 1.375;
        }
        else if(exercise.equals("3")){
            bmr = bmr * 1.55;
        }
        else if(exercise.equals("4")){
            bmr = bmr * 1.725;
        }
        else {
           bmr = bmr * 1.9;
        }

        int bmr3 = (int) Math.round(bmr);
        

        return bmr3;
    }


    //Method for creating healthy habits for the health page
    public void createHealthyHabits(ArrayList<healthyHabits> habitList){
        ArrayList<healthyHabits> list = habitList;

        boolean createNewHabits = newDayTester(1); //Run NewDayTester method to find if new day has started

        if(createNewHabits){
            //Make Sure Habits Chosen Are Not The Same
            int chosen = 0;
            int index1 = -1;
            int index2 = -1;

            while (chosen < 1){
                index1 = (int)(Math.random() * list.size());
                index2 = (int)(Math.random() * list.size());
                chosen++;
                if(index1 == index2){
                    chosen--;
                }
            }

            SharedPreferences userData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userData.edit();
            editor.putInt("index1", index1);
            editor.putInt("index2", index2);
            editor.apply();

            //Habits Selected And Now Being Sent To Print
            healthyHabits selection =  list.get(index1);
            createHabit(selection);

            healthyHabits selection2 =  list.get(index2);
            createHabit2(selection2);
        }
        else {
            SharedPreferences userData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
            int indexFirst = userData.getInt("index1", -1);
            int indexSecond = userData.getInt("index2", -1);

            healthyHabits selection =  list.get(indexFirst);
            createHabit(selection);

            healthyHabits selection2 =  list.get(indexSecond);
            createHabit2(selection2);
        }
    }


    //Printing Method For Habit 1
    public void createHabit(healthyHabits randChoice){
        TextView name;
        name = findViewById(R.id.healthyHabit10);

        String name1 = randChoice.getName();
        name.setText(name1);

        //Set Description
        String description = randChoice.getDescription();
        TextView descriptionText;
        descriptionText = findViewById(R.id.healthyHabit6);
        descriptionText.setText(description);

        //Write points
        int getPoints = randChoice.getPointValue();
        String printPoints = String.valueOf(getPoints);
        TextView pointsPrinter = (TextView)findViewById(R.id.healthyHabit7);
        pointsPrinter.setText(printPoints + " Block");

        //Set Image
        SharedPreferences userData3 = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        Boolean x = userData3.getBoolean("healthyHabitChecker1", false);
        if(!x){
            int imageHabit1 = randChoice.getHabitImage();
            ImageButton habitImage1 = findViewById(R.id.habitImage4);
            habitImage1.setImageResource(imageHabit1);
        }
        else{
            int imageHabit1 = R.drawable.checkmark;
            ImageButton habitImage1 = findViewById(R.id.habitImage4);
            habitImage1.setImageResource(imageHabit1);
        }
    }

    //Printing Method For Habit 2
    public void createHabit2(healthyHabits randChoice){
        //Set Name
        TextView name;
        name = findViewById(R.id.healthyHabit3);
        String name1 = randChoice.getName();
        name.setText(name1);

        //Set Description
        String description = randChoice.getDescription();
        TextView descriptionText;
        descriptionText = findViewById(R.id.healthyHabit);
        descriptionText.setText(description);

        //Set Points
        int getPoints = randChoice.getPointValue();
        String printPoints = String.valueOf(getPoints);
        TextView pointsPrinter = (TextView)findViewById(R.id.healthyHabit9);
        pointsPrinter.setText(printPoints + " Block");

        //Set Image
        SharedPreferences userData3 = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        Boolean x = userData3.getBoolean("healthyHabitChecker2", false);
        if(!x){
            int imageHabit1 = randChoice.getHabitImage();
            ImageButton habitImage1 = findViewById(R.id.habitImage2);
            habitImage1.setImageResource(imageHabit1);
        }
        else{
            int imageHabit1 = R.drawable.checkmark;
            ImageButton habitImage1 = findViewById(R.id.habitImage2);
            habitImage1.setImageResource(imageHabit1);
        }
    }

    //Check For Date And Return A Boolean
    public boolean newDayTester(int decider){

        //Get Day
        Date thisDate = new Date();
        SimpleDateFormat dateForm = new SimpleDateFormat("dd");
        String day = dateForm.format(thisDate);

        //Get Month
        Date thisDate2 = new Date();
        SimpleDateFormat dateForm2 = new SimpleDateFormat("MM");
        String month = dateForm2.format(thisDate2);

        //Get Year
        Date thisDate3 = new Date();
        SimpleDateFormat dateForm3 = new SimpleDateFormat("yyyy");
        String numericalYear = dateForm3.format(thisDate3);

        //Combine to make unique code
        String newDayTester = day + month + numericalYear;

        //Get previous newDayTester for comparison
        SharedPreferences userData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String date;
        if(decider == 1){
            date = userData.getString("previousDate", "");
        }
        else if(decider == 2){
            date = userData.getString("healthyHabit1PreviousDate", "");
        }
        else{
            date = userData.getString("healthyHabit2PreviousDate", "");
        }


        //Test to see if it is a new day
        if (date.equals(newDayTester)){
            return false;
        }
        else {
            SharedPreferences.Editor editor = userData.edit();
            if(decider == 1){
                editor.putString("previousDate", newDayTester);
                editor.putBoolean("healthyHabitChecker1", false);
                editor.putBoolean("healthyHabitChecker2", false);
            }
            else if(decider == 2){
                editor.putString("healthyHabit1PreviousDate", newDayTester);
            }
            else{
                editor.putString("healthyHabit2PreviousDate", newDayTester);
            }
            editor.apply();
            return true;
        }
    }

    //Sets Congrat Page For Habit 1
    public void congratsSetter1(View v){
        //Get Data including animation
        LottieAnimationView lottie;
        lottie = findViewById(R.id.lottie);
        SharedPreferences userData3 = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int firstIndex = userData3.getInt("index1", -1);
        healthyHabits selectedHealthyHabit = list.get(firstIndex);

        //Get habit data into variables
        String healthyHabitName = selectedHealthyHabit.getName();
        int points = selectedHealthyHabit.getPointValue();
        String pointToString = String.valueOf(points);
        int image = selectedHealthyHabit.getHabitImage();

        //Find and assign xml items
        TextView title = findViewById(R.id.habitTitle12);
        TextView points3 = findViewById(R.id.PointCounter);
        ImageView x = findViewById(R.id.habitImage3);

        //Set items
        points3.setText("+ " + pointToString + " Block");
        title.setText(healthyHabitName);
        x.setImageResource(image);

        //Points to account transfer
        int userPoints6 = userData3.getInt("userPoints", -1);
        userPoints6 += points;
        SharedPreferences.Editor editor = userData3.edit();
        editor.putInt("userPoints", userPoints6);
        editor.putBoolean("healthyHabitChecker1", true);
        editor.apply();

        //Change Habit Image To CheckMark
        ImageButton habitImage16 = findViewById(R.id.habitImage4);
        int checkMark = R.drawable.checkmark;
        habitImage16.setImageResource(checkMark);
    }

    //Sets Congrat Page For Habit 2
    public void congratsSetter2(View v){
        //Get Data including animation
        LottieAnimationView lottie;
        lottie = findViewById(R.id.lottie);
        SharedPreferences userData3 = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int secondIndex = userData3.getInt("index2", -1);
        healthyHabits selectedHealthyHabit = list.get(secondIndex);

        //Get habit data into variables
        String healthyHabitName = selectedHealthyHabit.getName();
        int points = selectedHealthyHabit.getPointValue();
        String pointToString = String.valueOf(points);
        int image = selectedHealthyHabit.getHabitImage();

        //Find and assign xml items
        TextView title = findViewById(R.id.habitTitle12);
        TextView points3 = findViewById(R.id.PointCounter);
        ImageView x = findViewById(R.id.habitImage3);

        //Set items
        points3.setText("+ " + pointToString + " Block");
        title.setText(healthyHabitName);
        x.setImageResource(image);

        //Points to account transfer
        int userPoints6 = userData3.getInt("userPoints", -1);
        userPoints6 += points;
        SharedPreferences.Editor editor = userData3.edit();
        editor.putInt("userPoints", userPoints6);
        editor.putBoolean("healthyHabitChecker2", true);
        editor.apply();

        //Change Habit Image To CheckMark
        ImageButton habitImage16 = findViewById(R.id.habitImage2);
        int checkMark = R.drawable.checkmark;
        habitImage16.setImageResource(checkMark);
    }

    //Runs On Click of Habit 1
    public void onClick1(View v){
        if(newDayTester(2)){
            showView(firstLayout);
            congratsSetter1(v);
        }
        else{
            Toast.makeText(Health.this, "Already Completed For Today!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //Runs On Click of Habit 2
    public void onClick2(View v){
        if(newDayTester(3)){
            showView(firstLayout);
            congratsSetter2(v);
        }
        else{
            Toast.makeText(Health.this, "Already Completed For Today!",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void backClick2(View v){
        hideView(firstLayout);
    }



}


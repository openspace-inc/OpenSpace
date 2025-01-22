package com.example.iaso;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.PersonalPage.DynamicHabit;
import com.example.iaso.PersonalPage.PersonalPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AddDynamicHabit extends AppCompatActivity {

    TextView title;
    TextView information;
    Button enter;
    Button secondary, payButton;
    EditText informationBox;
    int choice;
    int time, amount; //move to habits
    CardView informationCard, purchaseCard;
    String habitName, habitDescription; //move to habits
    TextView habitNameInfo, habitDescriptionInfo, timeInvestedInfo, amountInfo, autoAssistInfo1;
    TextView cost, balance;
    SharedPreferences userData, dynamicHabits;
    ImageButton exitButton;

    public ArrayList<DynamicHabit> dynamicHabitList;

    NumberPicker investmentNumber;

    LottieAnimationView background, clickAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_dynamic_habit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize all items
        title = findViewById(R.id.titleAddDynamicHabits);
        information = findViewById(R.id.information);
        enter = findViewById(R.id.enterButton);
        secondary = findViewById(R.id.secondaryButton);
        informationBox = findViewById(R.id.informationBox);
        background = findViewById(R.id.background);
        informationCard = findViewById(R.id.checkInformation);


        //data shown at the end.
        habitNameInfo = findViewById(R.id.nameOfHabit);
        habitDescriptionInfo = findViewById(R.id.descriptionOfHabit);
        timeInvestedInfo = findViewById(R.id.timeInvested);
        amountInfo = findViewById(R.id.amountOfHabit);
        autoAssistInfo1 = findViewById(R.id.autoAssistInfo);
        exitButton = findViewById(R.id.exitButton);

        //Purchase card initalization
        purchaseCard = findViewById(R.id.purchaseCard);
        cost = findViewById(R.id.cost);
        balance = findViewById(R.id.balance);
        payButton = findViewById(R.id.payButton);

        investmentNumber = findViewById(R.id.investmentPicker);
        investmentNumber.setVisibility(View.INVISIBLE);
        investmentNumber.setMaxValue(100);
        investmentNumber.setMinValue(0);

        Intent b = new Intent(this, MainActivity.class);

        investmentNumber.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                time = investmentNumber.getValue() * 5;
                titleChanger(time);
            }
        });


        //Clear text upon clicking information box.
        informationBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    informationBox.setText("");  // Clear the text
                }
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice++;
                options(choice);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(b);
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHabit();
            }
        });


        choice = 1;

        options(choice);

    }

    @SuppressLint("SetTextI18n")
    private void options(int optionNumber){

        //Initialize Animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation liftInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation.setStartOffset(150);
        liftInAnimation.setStartOffset(200);
        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        Animation liftInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation2.setStartOffset(800);
        liftInAnimation2.setStartOffset(850);

        //Initializing lottiefiles
        LottieAnimationView animationView = findViewById(R.id.autoassist);
        animationView.setVisibility(View.INVISIBLE);



        if (optionNumber == 1){
            //Initialize secondary button as invisible
            secondary.setVisibility(View.INVISIBLE);

            title.setText("Welcome");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            information.setText("Let's Create A Dynamic Habit. Together!");
            information.startAnimation(fadeInAnimation2); //Use any item in place of toDoList.
            information.startAnimation(liftInAnimation2);

            //Running lottiefiles
            animationView.setVisibility(View.VISIBLE);
            animationView.setAnimation(R.raw.floating);
            animationView.setSpeed(0.70f);
            animationView.playAnimation();

            informationBox.setVisibility(View.INVISIBLE);
            background.setVisibility(View.INVISIBLE);
            informationCard.setVisibility(View.INVISIBLE);

            purchaseCard.setVisibility(View.INVISIBLE);
        }
        else if (optionNumber == 2){
            //Set lottiefile to invisible
            animationView.setVisibility(View.INVISIBLE);
            background.setVisibility(View.VISIBLE);

            title.setText("Enter Your Objective");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            information.setText("Enter the name of the habit you want to create!");
            information.startAnimation(fadeInAnimation2); //Use any item in place of toDoList.
            information.startAnimation(liftInAnimation2);

            informationBox.setVisibility(View.VISIBLE);
            background.setVisibility(View.VISIBLE);

        }
        else if(optionNumber == 3){
            habitName = ((EditText)informationBox).getText().toString();
            title.setText("Description Please?");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            information.setText("Enter the information about, end goal, or why you are doing this habit.");
            information.startAnimation(fadeInAnimation2); //Use any item in place of toDoList.
            information.startAnimation(liftInAnimation2);

            informationBox.setText("");
        }
        else if (optionNumber == 4){
            //Collect data for description
            habitDescription = ((EditText)informationBox).getText().toString();

            informationBox.setText("");
            title.setText("Amount?");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            information.setText("What is the amount of this habit that is required? Ex. 3 Cups of Water. Just Enter The Number!");
            information.startAnimation(fadeInAnimation2); //Use any item in place of toDoList.
            information.startAnimation(liftInAnimation2);
            informationBox.setVisibility(View.VISIBLE);
        }
        else if(optionNumber == 5){
            //Get amount data and record
            String amount2 = ((EditText)informationBox).getText().toString();
            amount = Integer.parseInt(amount2);

            title.setText("Time For Your Investment");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            information.setText("Enter the daily amount of time you want to dedicate toward this habit");
            information.startAnimation(fadeInAnimation2); //Use any item in place of toDoList.
            information.startAnimation(liftInAnimation2);

            numberClicker();
            informationBox.setVisibility(View.INVISIBLE);
            background.setVisibility(View.INVISIBLE);
        }
        else if(optionNumber == 6){
            //Initialize secondary as visible so that user can skip
            secondary.setVisibility(View.VISIBLE);
            secondary.setText("In Development");
            enter.setText("Skip");

            title.setText("Auto Assist");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            enter.startAnimation(fadeInAnimation);
            enter.startAnimation(liftInAnimation);
            secondary.startAnimation(fadeInAnimation);
            secondary.startAnimation(liftInAnimation);

            information.setText("Auto Assist is an AI assistant that automatically reads your habit data" +
                    " and changes amounts based on how well you are doing. This will keep you on a path of improvement.");

            animationView.setVisibility(View.VISIBLE);
            animationView.setAnimation(R.raw.aiart2);// Replace with your animation file name
            animationView.setSpeed(0.70f);
            animationView.playAnimation();

            informationBox.setVisibility(View.INVISIBLE);
            investmentNumber.setVisibility(View.INVISIBLE);

            information.getPaint().setShader(null);
            information.setTextColor(Color.GRAY);

            information.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }
        else if (optionNumber == 7){
            title.setText("Everything Look Correct?");
            title.startAnimation(fadeInAnimation); //Use any item in place of toDoList.
            title.startAnimation(liftInAnimation);

            informationCard.setVisibility(View.VISIBLE);
            informationCard.startAnimation(fadeInAnimation);
            informationCard.startAnimation(liftInAnimation);

            habitNameInfo.setText(habitName);
            habitDescriptionInfo.setText(habitDescription);
            timeInvestedInfo.setText(time + " minutes");
            amountInfo.setText(amount + " units");
            autoAssistInfo1.setText("Auto Assist Not Added");

            information.setText("This is what your habit data looks like. Please make sure everything looks good before proceeding.");


            secondary.setText("Restart");
            enter.setText("Proceed To Payment");

            secondary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    choice = 1;
                    options(choice);

                }
            });
        }
        else if (optionNumber == 8){
            purchaseCard.setVisibility(View.VISIBLE);
            purchaseCard.startAnimation(fadeInAnimation);
            purchaseCard.startAnimation(liftInAnimation);

            userData = getSharedPreferences("UserData", Context.MODE_MULTI_PROCESS);
            int blockAmount = userData.getInt("userPoints", 0);
            String amount = Integer.toString(blockAmount);

            cost.setText("50 blocks"); // need to build dynamic version of cost
            balance.setText("balance: " + amount + " blocks");

        } else if (optionNumber == 9) {
            informationCard.setVisibility(View.INVISIBLE);
            purchaseCard.setVisibility(View.INVISIBLE);
            //background.setVisibility(View.VISIBLE);

            //background.setAnimation("checkmarkgreen.json");
            //background.playAnimation();

            openPage();

        } else{
            title.setText("Error 01");
        }


    }

    public void titleChanger(int x){
        String str = Integer.toString(x);
        information.setText(str + " minutes/day");
        String exampleText = "TestRun";

        information.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);

        //Gradient Color
        TextPaint paint = information.getPaint();
        float width = paint.measureText(String.valueOf(exampleText));

        Shader textShader = new LinearGradient(0, 0, width, information.getTextSize(),
                new int[]{
                        Color.parseColor("#de6262"),
                        Color.parseColor("#ffb88c"),
                }, null, Shader.TileMode.CLAMP);
        information.getPaint().setShader(textShader);
    }

    public void numberClicker(){
        investmentNumber.setVisibility(View.VISIBLE);
        investmentNumber.setMaxValue(96);
        investmentNumber.setMinValue(0);
        int size = (480 / 5) + 1;
        String[] timeAmounts = new String[size];

        for (int i = 0; i < size; i++) {
            timeAmounts[i] = String.valueOf(i * 5);
        }

        investmentNumber.setDisplayedValues(timeAmounts);
    }

    public void addHabit(){
        userData = getSharedPreferences("UserData", Context.MODE_MULTI_PROCESS);
        int blockAmount = userData.getInt("userPoints", 0);
        if(blockAmount < 50){
            balance.setText("Not enough blocks in account");
        }
        else{
            // Step 1: Retrieve the JSON string from SharedPreferences
            dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS); //Change sharedPreference Collection 😀
            Gson gson = new Gson();
            String json = dynamicHabits.getString("personalHabitList", null);

            //Checking to see if there is an error in json.
            Log.d("TESTINGFORJSON", "JSON before saving: " + json);

            // Step 2: Check to see if JSON = null
            if (json == null){
                //Create arrayList and add habits to it
                dynamicHabitList = new ArrayList<DynamicHabit>();
                DynamicHabit newHabit = new DynamicHabit(habitName, 0, "Personal", habitDescription, amount, time, R.drawable.calmshades, 0);
                dynamicHabitList.add(newHabit);

                String updatedJson = gson.toJson(dynamicHabitList);

                //Input new arrayList to sharedPrefs
                SharedPreferences.Editor dynamicHabitEditor = dynamicHabits.edit();
                dynamicHabitEditor.putString("personalHabitList", updatedJson);
                dynamicHabitEditor.apply();

                title.setText("Success! Made new arraylist");


                options(9);
            }
            else{
                Type type = new TypeToken<ArrayList<DynamicHabit>>(){
                }.getType();

                dynamicHabitList = gson.fromJson(json, type);

                DynamicHabit newHabit = new DynamicHabit(habitName, 0, "Personal", habitDescription, amount, time, R.drawable.calmshades, 0);
                dynamicHabitList.add(newHabit);

                // Step 5: Convert the updated list back to a JSON string
                String updatedJson = gson.toJson(dynamicHabitList);

                // Step 6: Save the updated JSON string back into SharedPreferences
                SharedPreferences.Editor dynamicHabitEditor = dynamicHabits.edit();
                dynamicHabitEditor.putString("personalHabitList", updatedJson);
                dynamicHabitEditor.apply();

                title.setText("Success!");
                Log.d("TestingForJSON", "JSON after saving: " + updatedJson);

                informationCard.setVisibility(View.INVISIBLE);
                purchaseCard.setVisibility(View.INVISIBLE);
                Intent b =new Intent(this, PersonalPage.class);
                startActivity(b);
            }
        }
    }

    public void openPage(){
        Intent b = new Intent(this, PersonalPage.class);
        startActivity(b);
    }
}
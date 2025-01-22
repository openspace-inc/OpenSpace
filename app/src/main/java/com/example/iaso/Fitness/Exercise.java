package com.example.iaso.Fitness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iaso.Health.Health;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;

public class Exercise extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);


        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        //Animations
        Animation fadeInLiftAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);

        //Get user points with method below
        pointsCounter();

        TextView upcoming = findViewById(R.id.upcoming1);
        upcoming.setText("You Are Currently On The BETA Model. \n" +
                "IASO Will Release Spring 2024\n" +
                "and provide this feature and much more.\n" +
                "All BETA users will recieve IASO X\n" +
                "For Absolutely Free.\n" +
                "Thanks For Being With Us.");
        upcoming.startAnimation(fadeInLiftAnimation);

        ImageView beta = findViewById(R.id.betaLogo);
        beta.startAnimation(fadeInAnimation);
    }

    public void transferToPersonal(View v){
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
    }

    public void transferToHealth(View v){
        Intent c = new Intent(this, Health.class);
        startActivity(c);
    }



    public void pointsCounter(){
        SharedPreferences userData3 = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int userPoints = userData3.getInt("userPoints", -1);
        String userPointsX = String.valueOf(userPoints);
    }
}
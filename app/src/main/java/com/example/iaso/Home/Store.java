package com.example.iaso.Home;

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

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;

public class Store extends AppCompatActivity {

    TextView points;
    LottieAnimationView dynamicLogo3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        points = findViewById(R.id.yourPoints);

        SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int x = userData.getInt("userPoints", 0);
        points.setText(Integer.toString(x) + " Blocks");

        TextView upcoming = findViewById(R.id.upcoming2);
        upcoming.setText("You Are Currently On The BETA Model. \n" +
                "The IASO Store Will Release Early 2025\n" +
                "Allowing You To Spend Your Well Earned Blocks.\n" +
                "Additionally, All BETA Users Will Recieve IASO X\n" +
                "For Absolutely Free.\n" +
                "Thanks For Being With Us.");


        Animation fadeInLiftAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        upcoming.startAnimation(fadeInLiftAnimation);

        ImageView beta = findViewById(R.id.betaLogo3);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        beta.startAnimation(fadeInAnimation);

        //Dynamic logo Code
        dynamicLogo3 = findViewById(R.id.dynamicLogo);
        dynamicLogo3.setAnimation(R.raw.iasodynamiclogo);
        dynamicLogo3.setRepeatCount(LottieDrawable.INFINITE);
        dynamicLogo3.playAnimation();
    }

    public void goBack(View v){
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
    }
}
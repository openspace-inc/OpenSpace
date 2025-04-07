package com.example.iaso.Home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.iaso.R;

import java.util.ArrayList;
import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    //NEEDS TO BE WORKED ON
    //AS OF RIGHT NOW IT DOES NOT OPEN UP IN THE CORRECT TIME. BE SURE TO FIX.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3600);


        //Set loading animation for splash screen
        LottieAnimationView animationView = findViewById(R.id.lottieloading);
        animationView.setAnimation(R.raw.loadingicon);// Replace with your animation file name
        animationView.setSpeed(1.30f);
        animationView.playAnimation();

        //Set Dynamic Subtitling Text
        //setDynamicText();

        //Fullscreen Code
        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

    }

    public void setDynamicText(){
        //TextView dynamicText = findViewById(R.id.dynamicText);
        String dynamic;
        ArrayList<String> x = new ArrayList<>();
        x.add("Start Your Rise");
        x.add("Did You Know That Iaso Was The Greek God For Recovering From Illness");
        x.add("Be Sure To Check Out The Store!");
        x.add("This App Is Based On a Multitude of Research/Books To Give You The Best Chance Of Success!");
        x.add("Does Anyone Ever Read These?");
        x.add("Keep Working Hard. It Will Pay Off");
        x.add("Did You Know The A Next To The Points Stands For Accomplishment?");
        x.add("Work Hard. Earn Points. Spend Big. No Compromise. Ever.");

        Random random = new Random();
        int size = x.size();
        int randomNumber = random.nextInt((size - 0) + 1);
        dynamic = x.get(randomNumber);
        //dynamicText.setText(dynamic);

    }


}
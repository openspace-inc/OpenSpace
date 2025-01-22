package com.example.iaso.Introduction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.iaso.R;

public class WelcomeActivity extends AppCompatActivity {

    LottieAnimationView welcomeIntro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setFloatingButton();

        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        fadeInAnimation2.setStartOffset(1000);

        //Run entry Animation(gif file if needed)
        //ImageView gifImageView = findViewById(R.id.entryScreen);
        //gifImageView.startAnimation(fadeInAnimation2);
        //Glide.with(this).asGif().load(R.drawable.iasowelcomeintro).into(gifImageView);


        //Run entry Animation (JSON file)
        welcomeIntro = findViewById(R.id.entryAnimationLocation);
        welcomeIntro.setAnimation(R.raw.welcomeintro5);
        welcomeIntro.setRepeatCount(LottieDrawable.INFINITE);
        welcomeIntro.playAnimation();
    }

    private void setFloatingButton() {

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation.setStartOffset(250);

        Button x = findViewById(R.id.button2);
        x.startAnimation(fadeInAnimation);

    }

    public void goToNext(View v){
        Intent x = new Intent(this, WaitList.class);
        startActivity(x);
    }
}
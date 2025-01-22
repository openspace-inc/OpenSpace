package com.example.iaso.Introduction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.iaso.R;

public class WelcomeActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        fadeInAnimation2.setStartOffset(650);
        Animation fadeInlift = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        fadeInlift.setStartOffset(1400);

        ImageView gifImageView = findViewById(R.id.entryScreen2);
        gifImageView.startAnimation(fadeInAnimation2);
        Glide.with(this).asGif().load(R.drawable.iasointro2).into(gifImageView);

        animatePictures();

        TextView description = findViewById(R.id.textView15);
        description.setText("Iaso is an app built on the purpose of accelerating the improvement of your Health, Body, and Mind to make good personal health accessible and goals a reality.");
        description.startAnimation(fadeInlift);
    }

    private void animatePictures() {
        ImageView a = findViewById(R.id.imageView12);
        ImageView b = findViewById(R.id.imageView13);
        ImageView c = findViewById(R.id.imageView14);
        Button next = findViewById(R.id.nextButton);

        Animation fadeInAnimation2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation2.setStartOffset(1900);
        Animation fadeInAnimation3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation3.setStartOffset(2300);
        Animation fadeInAnimation4 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation4.setStartOffset(2800);
        Animation fadeInAnimation5 = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation5.setStartOffset(6000);

        a.startAnimation(fadeInAnimation2);
        b.startAnimation(fadeInAnimation3);
        c.startAnimation(fadeInAnimation4);
        next.startAnimation(fadeInAnimation5);
    }

    public void nextPage(View v){
        Intent b = new Intent(this, WelcomeActivity3.class);
        startActivity(b);
    }
}
package com.example.iaso.ToDoList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.iaso.R;

public class FullTaskView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_task_view);

        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        String name = getIntent().getStringExtra("TaskName");
        String date = getIntent().getStringExtra("DateName");

        TextView dateX = findViewById(R.id.dateText16);
        dateX.setText("Due Date: " + date);

        TextView nameX = findViewById(R.id.nameTextViewFull);
        nameX.setText(name);

        TextPaint paint = nameX.getPaint();
        float width = paint.measureText(name);

        Shader textShader = new LinearGradient(0, 0, width, nameX.getTextSize(),
                new int[]{
                        Color.parseColor("#de6262"),
                        Color.parseColor("#ffb88c"),
                }, null, Shader.TileMode.CLAMP);
        nameX.getPaint().setShader(textShader);
    }

    public void goBack3(View v){
        Intent x = new Intent(this, TaskLister.class);
        startActivity(x);
    }
}
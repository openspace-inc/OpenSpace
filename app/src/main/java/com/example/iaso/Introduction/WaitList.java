package com.example.iaso.Introduction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iaso.R;

public class WaitList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wait_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String code = "AXC4";

        ImageView checkMark = findViewById(R.id.checkmark);
        checkMark.setVisibility(View.GONE);

        TextView information = findViewById(R.id.information);

        EditText box1 = findViewById(R.id.code1);
        EditText box2 = findViewById(R.id.code2);
        EditText box3 = findViewById(R.id.code3);
        EditText box4 = findViewById(R.id.code4);

        Button submitButton = findViewById(R.id.submitButton);

        Handler handler = new Handler(Looper.getMainLooper());


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here
                String code = box1.getText().toString()
                        + box2.getText().toString()
                        + box3.getText().toString()
                        + box4.getText().toString();
                // Do something with 'code'
                if (code.equalsIgnoreCase("AXC4")){
                    checkMark.setVisibility(View.VISIBLE);
                    information.setText("Success! Proceeding Further...");

                    handler.postDelayed(() -> {
                        goToNext(v);
                    }, 1500);

                }
                else {
                    information.setText("Invalid Code. Didn't join the waitlist? Proceed below.");
                }
            }
        });

    }

    public void goToNext(View v){
        Intent x = new Intent(this, WelcomeActivity2.class);
        startActivity(x);
    }
}
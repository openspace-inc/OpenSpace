package com.example.iaso.ToDoList;

import static android.view.animation.AnimationUtils.loadAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;

import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class TaskLister extends AppCompatActivity implements RecyclerViewInterface {

    ImageButton mSaveBtn;
    ArrayList<TaskItem> tasks;
    EditText nameBox;
    EditText date;
    String dueDate;
    Context context;
    TaskLister_RecyclerViewAdapter adapter;
    View view;
    TextView suggestion;
    LottieAnimationView taskEmptyAnimation;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_lister);

        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        //Show User Instructions
        Boolean isFirstRun = getSharedPreferences("UserData", MODE_PRIVATE)
                .getBoolean("firstRunTaskLister", true);
        if(isFirstRun){
            Toast.makeText(TaskLister.this, "Click To Enter Expanded View \n Hold To Complete", Toast.LENGTH_LONG)
                    .show();
        }
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("firstRunTaskLister", false).apply();


        nameBox = findViewById(R.id.NameBoxEditText);
        date = findViewById(R.id.dateText);
        tasks = new ArrayList<>();

        //Investigate
        taskEmptyAnimation = findViewById(R.id.animationView9);
        taskEmptyAnimation.setAnimation(R.raw.emptyanimation);
        taskEmptyAnimation.playAnimation();
        taskEmptyAnimation.setVisibility(View.GONE);

        mSaveBtn = findViewById(R.id.plusIcon);
        recyclerView = findViewById(R.id.mRecyclerView);

        suggestion = findViewById(R.id.suggestionText);
        suggestion.setText("Get Started On A Task!");
        suggestion.setVisibility(View.GONE);


        //ON Press Of SaveBtn, save Data to ArrayList
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date.getText().toString().equals("Date")){
                    dueDate = "";
                    saveData(nameBox.getText().toString(), dueDate);
                }
                else{
                    saveData(nameBox.getText().toString(), date.getText().toString());
                    tasksEmpty();
                }
            }
        });

        //Clear EditText For User Upon Press
        nameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    nameBox.setText("");  // Clear the text
                }
            }
        });
        nameBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameBox.setText("");
            }
        });

        //Clear Date For User Upon Press
        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    date.setText("");  // Clear the text
                }
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date.setText("");
            }
        });

        loadData();

        adapter = new TaskLister_RecyclerViewAdapter(this, tasks, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tasksEmpty();
    }

    //Load Data Saved From ArrayList
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("taskData",null);

        if (json == null){
            tasksEmpty();
        }
        else {
            Type type = new TypeToken<ArrayList<TaskItem>>(){
            }.getType();

            tasks = gson.fromJson(json,type);

            TaskLister_RecyclerViewAdapter adapter = new TaskLister_RecyclerViewAdapter(this, tasks, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            tasksEmpty();
        }
    }
    //Save Data Collected From EditText To ArrayList in SharedPref
    public void saveData(String name, String date){
        SharedPreferences sharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        TaskItem taskItem = new TaskItem(name, date);
        tasks.add(taskItem);
        String json = gson.toJson(tasks);
        editor.putString("taskData", json);
        editor.apply();

        nameBox.getText().clear();
        nameBox.setText("Next Task?");

        tasksEmpty();
        loadData();

    }

    public void goBack(View v){
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(tasks);
        editor.putString("taskData", json);
        editor.apply();

        loadData();

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(TaskLister.this, FullTaskView.class);
        intent.putExtra("TaskName", tasks.get(position).getName());
        intent.putExtra("DateName", tasks.get(position).getDate());
        startActivity(intent);

    }

    @Override
    public void onItemLongClick(int position) {
        tasks.remove(position);
        adapter.notifyItemRemoved(position);
        saveData();
        tasksEmpty();

        //Add points to account
        SharedPreferences userData3 = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userData3.edit();
        int userPoints = userData3.getInt("userPoints", -1);
        userPoints += 10;
        editor.putInt("userPoints", userPoints);
        editor.apply();

        //Give Message To User That Points have been added
        Random random = new Random();
        int x = random.nextInt(10) + 1;
        if(x < 2){
            Toast.makeText(getApplicationContext(), "10 Points Added!", Toast.LENGTH_SHORT).show();
        }
        else if (x < 4){
            Toast.makeText(getApplicationContext(), "Keep It Up!", Toast.LENGTH_SHORT).show();
        }
        else if (x < 6){
            Toast.makeText(getApplicationContext(), "Nicely Done!", Toast.LENGTH_SHORT).show();
        }
        else if(x < 7){
            Toast.makeText(getApplicationContext(), "One More Task Done! Ready To Add Another?", Toast.LENGTH_SHORT).show();
        }
        else if (x < 9){
            Toast.makeText(getApplicationContext(), "Let's Go!!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "10 Points. Congrats.", Toast.LENGTH_SHORT).show();
        }


    }

    public void tasksEmpty(){
        taskEmptyAnimation.setAnimation(R.raw.emptyanimation);
        taskEmptyAnimation.playAnimation();

        if (tasks.size() == 0) {
            // Replace with your Lottie animation file
            taskEmptyAnimation.setVisibility(View.VISIBLE);
            //Animation for textView
            suggestion.setVisibility(View.VISIBLE);
            Animation fadeInAnimation = loadAnimation(this, R.anim.fade_in_animation);
            suggestion.startAnimation(fadeInAnimation);
        } else {
            // Condition not met, show a toast or handle accordingly
            taskEmptyAnimation.setVisibility(View.GONE);
            suggestion.setVisibility(View.GONE);
        }
    }



}
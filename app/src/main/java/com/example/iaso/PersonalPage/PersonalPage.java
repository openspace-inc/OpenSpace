package com.example.iaso.PersonalPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.AddDynamicHabit;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.R;
import com.example.iaso.ToDoList.RecyclerViewInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PersonalPage extends AppCompatActivity implements RecyclerViewInterface {

    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>();
    SharedPreferences dynamicHabits;
    public ArrayList<DynamicHabit> personalHabitsList = new ArrayList<DynamicHabit>();

    RecyclerView displayPersonalHabits;
    ImageButton exitButton;

    SharedPreferences personalHabits1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView Title = findViewById(R.id.PersonalPageHeader);
        String title = "Personal";
        Title.setText(title);

        //Initialize recycler view
        displayPersonalHabits = findViewById(R.id.personalHabitDisplayRecyclerView);

        //Recieve SharedPrefs
        setUpPersonalHabits();

        //Press to add habit
        Button addContract = findViewById(R.id.addHabitButton);
        Intent b = new Intent(this, AddDynamicHabit.class);
        addContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(b);
            }
        });

        //Exit the activity
        exitButton = findViewById(R.id.exitButtonForPersonalPage);

            exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent b = new Intent(PersonalPage.this, MainActivity.class);
                startActivity(b);
            }
        });
    }

    //Recieve SharedPrefs and check if there is anything in there
    private void setUpPersonalHabits() {
        dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS);
        String json = dynamicHabits.getString("personalHabitList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        dynamicHabitList = gson.fromJson(json,type);

        if (dynamicHabitList == null) {
            tasksEmpty();
            displayPersonalHabits.setVisibility(View.INVISIBLE);
        }
        else {
            PersonalHabit_RecyclerViewAdapter recyclerViewAdapter = new PersonalHabit_RecyclerViewAdapter(this, dynamicHabitList, this);
            displayPersonalHabits.setAdapter(recyclerViewAdapter);
            displayPersonalHabits.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    //Run if nothing in sharedprefs
    private void tasksEmpty() {
        TextView Title = findViewById(R.id.PersonalPageHeader);
        String title = "No Personal Habits Available. Sorry";
        Title.setText(title);
    }

    //Storing data for habits should be conducted here.
    @Override
    public void onItemClick(int position) {
        if (dynamicHabitList == null) {
            tasksEmpty();
        }
        else {
            String name = dynamicHabitList.get(position).getName3();
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        }
    }

    //Shows data analytics on a long press.
    @Override
    public void onItemLongClick(int position) {

    }
}
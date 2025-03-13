package com.example.iaso.PersonalPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    public ArrayList<dataStorage> dataStorageList = new ArrayList<dataStorage>();

    SharedPreferences dynamicHabits;
    SharedPreferences dataStorage;

    RecyclerView displayPersonalHabits;

    ImageButton exitButton;



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

    //Adds data storage object to sharedprefs
    private void addToDataStorage(dataStorage newData) {
        //initalize data storage
        dataStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE); //brought in file
        SharedPreferences.Editor dataEditor = dataStorage.edit(); //allowed editing of file

        String json = dataStorage.getString("userStorageList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        dataStorageList = gson.fromJson(json,type); //filled arraylist with stored data

        //debugging control: (displays data stored)
        for (dataStorage x : dataStorageList) {
            Log.d("ArrayListCheck", "Person Name: " + x.getName()); // Appears in Logcat
            Log.d("ArrayListCheck", "Person Type: " + x.getType());
            Log.d("ArrayListCheck", "Person Hours: " + x.getHours());
        }

        //prevents runtime errors
        if (dataStorageList == null) {
            dataStorageList = new ArrayList<dataStorage>();
        }
        else {
            String x = "just a placer";
        }

        dataStorageList.add(newData);
        String updatedJson = gson.toJson(dataStorageList);

        // Step 6: Save the updated JSON string back into SharedPreferences
        dataEditor.putString("userStorageList", updatedJson);
        dataEditor.apply();

        //debugging confirm that the habit has been stored
        Toast.makeText(getApplicationContext(), "Success. added the data", Toast.LENGTH_SHORT).show();
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
            //initalize variables that need to be stored
            String name = dynamicHabitList.get(position).getName3();
            int time = dynamicHabitList.get(position).getTime();

            dataStorage newData = new dataStorage(name, "Project", time);
            addToDataStorage(newData);  //on return make sure that this is working. check by printing out the arraylist t
            //that stores all datastorage.
        }
    }

    //Shows data analytics on a long press.
    @Override
    public void onItemLongClick(int position) {

    }
}
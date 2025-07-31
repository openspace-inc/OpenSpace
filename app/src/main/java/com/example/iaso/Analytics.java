package com.example.iaso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iaso.PersonalPage.PersonalPage;
import com.example.iaso.PersonalPage.dataStorage;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.lang.reflect.Type;
import java.util.ArrayList;

//This page is to bring in all data from a specific project and display that data in a meaningful way through libraries
public class Analytics extends AppCompatActivity {

    String name1;
    String project_description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        name1 = getIntent().getStringExtra("stock_name");
        project_description = getIntent().getStringExtra("project_description");
        TextView name = findViewById(R.id.name);
        name.setText(name1);
        TextView description = findViewById(R.id.description);
        description.setText(project_description);

        ImageButton exitButton = findViewById(R.id.backbutton234);
        exitButton.setOnClickListener(view -> {
            Intent b = new Intent(Analytics.this, PersonalPage.class);
            startActivity(b);
        });

        totalHours();
        //totalHoursThisWeek();

        //set up recyclerview
        //dataPull();

        //build spark graph
        buildSparkGraph(name1);
    }

    void buildSparkGraph(String projectName) {
        SparkView sparkView = (SparkView) findViewById(R.id.sparkview);
        ArrayList<dataStorage> data = openSharedPref();

        //filter data
        double tempTime = 0;
        int lastDate = data.get(0).getDate();
        ArrayList<dataStorage> filteredData = new ArrayList<>();

        //Advanced loop to filter data
        for (int y = 0; y < data.size(); y++){
            dataStorage x = data.get(y);

            if (projectName != null && projectName.equals(x.getName())){

                //date not same add object
                if (lastDate != x.getDate()){
                    dataStorage temp = new dataStorage(data.get(y-1).getName(), data.get(y-1).getType(), tempTime, data.get(y-1).getDate());
                    filteredData.add(temp);

                    lastDate = x.getDate();
                    tempTime = x.getHours();

                    continue;
                }
                //if date same wait
                else {
                    tempTime += x.getHours();
                }

                dataStorage temp = new dataStorage(x.getName(), x.getType(), tempTime, x.getDate());
                filteredData.add(temp);
            }
        }

        MyAdapter adapter = new MyAdapter(filteredData);
        sparkView.setAdapter(adapter);
    }

    //opens sharedpref for data storage and returns the arraylist
    ArrayList<dataStorage> openSharedPref(){
        ArrayList<dataStorage> storage = new ArrayList<>();

        //Create userStorage arraylst for the app
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor dataStorage = userStorage.edit();


        String json = userStorage.getString("userStorageList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        storage = gson.fromJson(json,type);


        if (storage == null) {
            storage = new ArrayList<dataStorage>();
        }
        else {
            String x = "just a placer";
        }

        return storage;
    }

    //not in use currently - supposed to store sharedpref and close it
    void applySharedPref(ArrayList<dataStorage> dataArrayList){
        SharedPreferences userStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor dataStorage = userStorage.edit();

        //get ArrayList from shared preferences
        String json = userStorage.getString("userStorageList",null);
        Gson gson = new Gson();

        //Turning ArrayList into JSON and then applying.
        json = gson.toJson(dataArrayList);
        dataStorage.putString("userStorageList", json);
        dataStorage.apply();
    }

    //Displays total hours worked toward this project
    void totalHours(){
        //initalize shared preferences
        ArrayList<dataStorage> storage = openSharedPref();
        double hours = 0;

        for (dataStorage x : storage){
            if (x.getName().equals(name1)) {
                hours += x.getHours();
            }
        }

        TextView totalHours = findViewById(R.id.amountOfHours);
        totalHours.setText(String.format("%.2f", hours / 60));
    }

    public class MyAdapter extends SparkAdapter {
         private final ArrayList<dataStorage> data; //data type being fed in
     
         
         public MyAdapter(ArrayList<dataStorage> data) {
             this.data = data;
         }
     
         @Override
         public int getCount() {
             return data.size();
         }
     
         @Override
         public Object getItem(int index) {
            return data.get(index);
         }
     
         @Override
         public float getY(int index) {
           return (float) data.get(index).getHours();
         }

         @Override
        public float getX(int index){
             return (float) data.get(index).getDate();
         }
    }

}
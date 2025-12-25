package com.example.iaso.PersonalPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iaso.AddDynamicHabit;
import com.example.iaso.Analytics;
import com.example.iaso.BottomNavigationHelper;
import com.example.iaso.BrownianStockManager;
import com.example.iaso.Home.MainActivity;
import com.example.iaso.Model.StockDataPoint;
import com.example.iaso.R;
import com.example.iaso.ToDoList.RecyclerViewInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//This page is to display all personal projects the user has through a recyclerview interface
public class PersonalPage extends AppCompatActivity implements RecyclerViewInterface {


    //used to run animation only once
    private static boolean hasPlayedAnimation = false;
    CardView achievementCard;

    public ArrayList<DynamicHabit> dynamicHabitList = new ArrayList<DynamicHabit>(); //Stores List Of Projects
    SharedPreferences dynamicHabits;

    public ArrayList<dataStorage> dataStorageList = new ArrayList<dataStorage>(); //Stores Recorded Data Of Projects
    SharedPreferences dataStorage;

    RecyclerView displayPersonalHabits;

    ImageButton exitButton;
    TextView emptyText;

    // Portfolio spark graph components
    private SparkView portfolioSparkView;
    private final ArrayList<StockDataPoint> fullPortfolioHistory = new ArrayList<>();
    private Range currentRange = Range.DAY_1;
    private TextView portfolioValueView;
    private TextView portfolioPercentageView;
    private TextView portfolioRangeLabelView;
    private ImageView portfolioTrendArrowView;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        emptyText = findViewById(R.id.textView19);

        TextView Title = findViewById(R.id.PersonalPageHeader);
        String title = "Your Projects";
        Title.setText(title);

        //set the achievement cardview to invisible
        achievementCard = findViewById(R.id.AchievementPage);
        achievementCard.setVisibility(View.INVISIBLE);


        //Initialize recycler view
        displayPersonalHabits = findViewById(R.id.personalHabitDisplayRecyclerView);

        if (!hasPlayedAnimation){
            Animation upwardsFade = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
            displayPersonalHabits.startAnimation(upwardsFade);

            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
            fadeInAnimation.setStartOffset(300);
            Title.startAnimation(fadeInAnimation);

            hasPlayedAnimation = true;
        }

        //Ensure Brownian stock data is up to date before displaying
        BrownianStockManager.updateAll(this);

        //Recieve sharedpref for projects - apply to recyclerview (auto set up dynamichabits sharedpref)
        setUpPersonalHabits();

        // Initialize portfolio spark graph components
        portfolioValueView = findViewById(R.id.portfolioValue);
        portfolioPercentageView = findViewById(R.id.portfolioPercentage);
        portfolioRangeLabelView = findViewById(R.id.portfolioRangeLabel);
        portfolioTrendArrowView = findViewById(R.id.portfolioTrendArrow);
        portfolioSparkView = findViewById(R.id.portfolioSparkView);

        buildPortfolioSparkGraph();
        setupPortfolioRangeButtons();

        //Press to add habit
        ImageButton addContract = findViewById(R.id.addHabitButton);
        Intent b = new Intent(this, AddDynamicHabit.class);
        addContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(b);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

        // Setup bottom navigation bar
        BottomNavigationHelper.setupBottomNavigation(this, R.id.bottom_nav_include, PersonalPage.class);
    }

    //Add a data entry to the dataStorage sharedPref
    private void addToDataStorage(dataStorage newData) {
        dataStorage = getSharedPreferences("userStorage", Context.MODE_PRIVATE); //brought in file
        SharedPreferences.Editor dataEditor = dataStorage.edit(); //allowed editing of file

        String json = dataStorage.getString("userStorageList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<dataStorage>>(){}.getType();
        dataStorageList = gson.fromJson(json,type); //filled arraylist with stored data

        //Debugging control: (displays data stored)
        for (dataStorage x : dataStorageList) {
            Log.d("ArrayListCheck", "Person Name: " + x.getName()); // Appears in Logcat
            Log.d("ArrayListCheck", "Person Type: " + x.getType());
            Log.d("ArrayListCheck", "Person Hours: " + x.getHours());
            Log.d("ArrayListCheck", "Date:" + x.getDate());
        }

        //Prevents runtime errors
        if (dataStorageList == null) {
            dataStorageList = new ArrayList<dataStorage>();
        }
        else {
            String x = "just a placer";
        }

        //Addition of data entry to storage
        dataStorageList.add(newData);
        String updatedJson = gson.toJson(dataStorageList);

        achievementCard.setVisibility(View.VISIBLE);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_lift);
        fadeInAnimation.setStartOffset(300);
        achievementCard.startAnimation(fadeInAnimation);
        TextView minutesText = findViewById(R.id.minutesText);
        minutesText.setText(String.valueOf(newData.getHours()));

        //Save the updated JSON string back into SharedPreferences
        dataEditor.putString("userStorageList", updatedJson);
        dataEditor.apply();

        BrownianStockManager.onMinutesLogged(this, newData.getName());

        //Debugging confirm that the habit has been stored
        Toast.makeText(getApplicationContext(), "Success. added the data", Toast.LENGTH_SHORT).show();
    }

    //Open sharedpref for the projects and apply to recyclerview
    private void setUpPersonalHabits() {
        dynamicHabits = getSharedPreferences("PersonalHabits", Context.MODE_MULTI_PROCESS);
        String json = dynamicHabits.getString("personalHabitList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<DynamicHabit>>(){}.getType();
        dynamicHabitList = gson.fromJson(json,type);

        if (dynamicHabitList == null || dynamicHabitList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No Projects To Display. Build one using the square button above");
            displayPersonalHabits.setVisibility(View.INVISIBLE);
        }
        else {
            emptyText.setVisibility(View.INVISIBLE);
            PersonalHabit_RecyclerViewAdapter recyclerViewAdapter = new PersonalHabit_RecyclerViewAdapter(this, dynamicHabitList, this);
            displayPersonalHabits.setAdapter(recyclerViewAdapter);
            displayPersonalHabits.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    //Upon long pressing project, call the analytics page
    void callAnalyticsClass(int position){
        Intent b = new Intent(PersonalPage.this, Analytics.class);
        b.putExtra("project_name", dynamicHabitList.get(position).getName3());
        b.putExtra("stock_name", dynamicHabitList.get(position).getStockSymbol());
        b.putExtra("project_description", dynamicHabitList.get(position).getDescription());
        startActivity(b);
    }

    //Call storage of data entry
    @Override
    public void onItemClick(int position) {
        if (dynamicHabitList == null) {
            Toast.makeText(getApplicationContext(), "No projects to display", Toast.LENGTH_SHORT).show();
        }
        else {
            //Adds new entry to dataStorage ArrayList
            String name = dynamicHabitList.get(position).getName3();
            int time = dynamicHabitList.get(position).getTime();

            dataStorage newData = new dataStorage(name, "Project", time);
            addToDataStorage(newData);
        }
    }

    //Call analytics page
    @Override
    public void onItemLongClick(int position) {
         callAnalyticsClass(position);
    }

    //transfer to hub page
    public void goToHub(View view) {
        Intent b = new Intent(this, MainActivity.class);
        startActivity(b);
        overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
    }

    //this piece of code doesn't work
    public void exitAchievementText(View view){
        achievementCard.setVisibility(View.INVISIBLE);
    }

    // Portfolio Spark Graph Methods

    /**
     * Builds the portfolio spark graph by aggregating stock data from all projects
     */
    void buildPortfolioSparkGraph() {
        if (dynamicHabitList == null || dynamicHabitList.isEmpty()) {
            // No projects to display, hide the portfolio overview
            findViewById(R.id.portfolioOverview).setVisibility(View.GONE);
            return;
        }

        // Show the portfolio overview
        findViewById(R.id.portfolioOverview).setVisibility(View.VISIBLE);

        // Aggregate portfolio data from all projects
        ArrayList<StockDataPoint> portfolioData = aggregatePortfolioHistory();

        fullPortfolioHistory.clear();
        if (!portfolioData.isEmpty()) {
            // Keep the history sorted by timestamp
            Collections.sort(portfolioData, Comparator.comparingLong(StockDataPoint::getTimestamp));
            fullPortfolioHistory.addAll(portfolioData);
        }

        updatePortfolioSparkForRange(currentRange);
    }

    /**
     * Aggregates stock price history from all projects by summing prices at the same timestamps
     */
    private ArrayList<StockDataPoint> aggregatePortfolioHistory() {
        Map<Long, Double> timestampToPriceSum = new HashMap<>();

        // Iterate through all projects and collect their price history
        for (DynamicHabit habit : dynamicHabitList) {
            String habitName = habit.getName3();
            ArrayList<StockDataPoint> habitHistory = BrownianStockManager.getHistoryForHabit(this, habitName);

            // Sum up prices at each timestamp
            for (StockDataPoint point : habitHistory) {
                long timestamp = point.getTimestamp();
                double price = point.getPrice();
                timestampToPriceSum.put(timestamp, timestampToPriceSum.getOrDefault(timestamp, 0.0) + price);
            }
        }

        // Convert the map to a list of StockDataPoint objects
        ArrayList<StockDataPoint> aggregatedData = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : timestampToPriceSum.entrySet()) {
            aggregatedData.add(new StockDataPoint("Portfolio", entry.getKey(), entry.getValue()));
        }

        return aggregatedData;
    }

    /**
     * Sets up the time range buttons for the portfolio graph
     */
    private void setupPortfolioRangeButtons() {
        RadioGroup rangeGroup = findViewById(R.id.portfolioRangeGroup);
        if (rangeGroup == null) {
            return;
        }

        // When the user taps a new period button, refresh the chart and summary text.
        rangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Range selected = mapPortfolioRangeFromId(checkedId);
            if (selected != null && selected != currentRange) {
                currentRange = selected;
                updatePortfolioSparkForRange(currentRange);
            }
        });

        // Default to showing the most recent day.
        rangeGroup.check(R.id.portfolioRange24h);
    }

    /**
     * Maps radio button IDs to Range enum values
     */
    private Range mapPortfolioRangeFromId(int checkedId) {
        if (checkedId == R.id.portfolioRange24h) {
            return Range.DAY_1;
        } else if (checkedId == R.id.portfolioRange3d) {
            return Range.DAY_3;
        } else if (checkedId == R.id.portfolioRange1w) {
            return Range.WEEK_1;
        } else if (checkedId == R.id.portfolioRange1m) {
            return Range.MONTH_1;
        } else if (checkedId == R.id.portfolioRange3m) {
            return Range.MONTH_3;
        } else if (checkedId == R.id.portfolioRange1y) {
            return Range.YEAR_1;
        } else if (checkedId == R.id.portfolioRangeAll) {
            return Range.ALL;
        }
        return null;
    }

    /**
     * Updates the portfolio spark graph for the selected time range
     */
    private void updatePortfolioSparkForRange(Range range) {
        if (portfolioSparkView == null) {
            return;
        }
        ArrayList<StockDataPoint> display = filterPortfolioHistoryForRange(range);
        portfolioSparkView.setAdapter(new PortfolioSparkAdapter(display));
        updatePortfolioSummaryViews(display, range);
    }

    /**
     * Filters portfolio history to show only data points within the selected time range
     */
    private ArrayList<StockDataPoint> filterPortfolioHistoryForRange(Range range) {
        if (fullPortfolioHistory.isEmpty()) {
            return new ArrayList<>();
        }

        if (range == Range.ALL) {
            return new ArrayList<>(fullPortfolioHistory);
        }

        long windowMillis = range.getWindowMillis();
        if (windowMillis <= 0L) {
            return new ArrayList<>(fullPortfolioHistory);
        }

        StockDataPoint latest = fullPortfolioHistory.get(fullPortfolioHistory.size() - 1);
        long cutoff = latest.getTimestamp() - windowMillis;

        ArrayList<StockDataPoint> filtered = new ArrayList<>();
        for (StockDataPoint point : fullPortfolioHistory) {
            if (point.getTimestamp() >= cutoff) {
                filtered.add(point);
            }
        }
        if (filtered.isEmpty()) {
            filtered.add(fullPortfolioHistory.get(fullPortfolioHistory.size() - 1));
        }
        return filtered;
    }

    /**
     * Updates the summary views (portfolio value, percentage change, trend arrow)
     */
    private void updatePortfolioSummaryViews(ArrayList<StockDataPoint> display, Range range) {
        if (portfolioValueView == null || portfolioPercentageView == null ||
            portfolioRangeLabelView == null || portfolioTrendArrowView == null) {
            return;
        }

        if (display == null || display.isEmpty()) {
            // Nothing to display yet, reset to defaults
            portfolioValueView.setText(currencyFormat.format(0d));
            portfolioPercentageView.setText(String.format(Locale.getDefault(), "%+.2f%%", 0f));
            portfolioRangeLabelView.setText(String.format(Locale.getDefault(), "VS %s", range.getLabel()));
            portfolioTrendArrowView.setImageResource(R.drawable.os5_uparrow1);
            return;
        }

        StockDataPoint latest = display.get(display.size() - 1);
        double latestValue = latest.getPrice();
        portfolioValueView.setText(currencyFormat.format(latestValue));

        StockDataPoint referencePoint = findPortfolioReferencePoint(range, latest);
        double referenceValue = referencePoint != null ? referencePoint.getPrice() : latestValue;
        double percentChange = referenceValue == 0d ? 0d : ((latestValue - referenceValue) / referenceValue) * 100d;

        portfolioPercentageView.setText(String.format(Locale.getDefault(), "%+.2f%%", percentChange));
        portfolioRangeLabelView.setText(String.format(Locale.getDefault(), "VS %s", range.getLabel()));
        portfolioTrendArrowView.setImageResource(percentChange < 0d ? R.drawable.os5_downarrow2 : R.drawable.os5_uparrow1);
    }

    /**
     * Finds the reference point for calculating percentage change
     */
    private StockDataPoint findPortfolioReferencePoint(Range range, StockDataPoint latest) {
        if (fullPortfolioHistory.isEmpty() || latest == null) {
            return null;
        }

        if (range == Range.ALL) {
            // Compare to the very first recorded value when "All" is active.
            return fullPortfolioHistory.get(0);
        }

        long windowMillis = range.getWindowMillis();
        if (windowMillis <= 0L) {
            return fullPortfolioHistory.get(0);
        }

        long target = latest.getTimestamp() - windowMillis;
        StockDataPoint before = null;
        for (StockDataPoint point : fullPortfolioHistory) {
            long timestamp = point.getTimestamp();
            if (timestamp == target) {
                return point;
            }
            if (timestamp < target) {
                before = point;
                continue;
            }
            if (before == null) {
                return point;
            }
            long diffBefore = target - before.getTimestamp();
            long diffAfter = timestamp - target;
            return diffAfter < diffBefore ? point : before;
        }
        return before != null ? before : fullPortfolioHistory.get(0);
    }

    /**
     * Enum representing different time ranges for the portfolio graph
     */
    private enum Range {
        DAY_1("1D", 24L * 60L * 60L * 1000L),
        DAY_3("3D", 3L * 24L * 60L * 60L * 1000L),
        WEEK_1("1W", 7L * 24L * 60L * 60L * 1000L),
        MONTH_1("1M", 30L * 24L * 60L * 60L * 1000L),
        MONTH_3("3M", 90L * 24L * 60L * 60L * 1000L),
        YEAR_1("1Y", 365L * 24L * 60L * 60L * 1000L),
        ALL("ALL", -1L);

        private final String label;
        private final long windowMillis;

        Range(String label, long windowMillis) {
            this.label = label;
            this.windowMillis = windowMillis;
        }

        long getWindowMillis() {
            return windowMillis;
        }

        String getLabel() {
            return label;
        }
    }

    /**
     * Adapter for the portfolio spark graph
     */
    public class PortfolioSparkAdapter extends SparkAdapter {
        private final ArrayList<StockDataPoint> data;
        private final long baseTimestamp;

        public PortfolioSparkAdapter(ArrayList<StockDataPoint> data) {
            this.data = data;
            if (data.isEmpty()) {
                baseTimestamp = 0L;
            } else {
                baseTimestamp = data.get(0).getTimestamp();
            }
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
            return (float) data.get(index).getPrice();
        }

        @Override
        public float getX(int index){
            long timestamp = data.get(index).getTimestamp();
            if (baseTimestamp == 0L) {
                return index;
            }
            return (float) ((timestamp - baseTimestamp) / 3600000f);
        }
    }

}
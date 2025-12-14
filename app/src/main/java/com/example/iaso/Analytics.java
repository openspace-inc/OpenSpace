package com.example.iaso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iaso.Model.StockDataPoint;
import com.example.iaso.PersonalPage.PersonalPage;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

//This page is to bring in all data from a specific project and display that data in a meaningful way through libraries
public class Analytics extends AppCompatActivity {

    String name1;
    String project_description;
    private SparkView sparkView;
    private final ArrayList<StockDataPoint> fullHistory = new ArrayList<>();
    private Range currentRange = Range.DAY_1;
    private TextView currentPriceView;
    private TextView percentageView;
    private TextView rangeLabelView;
    private ImageView trendArrowView;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
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

        // Collect the project details passed to this page so we can show the correct labels.
        name1 = getIntent().getStringExtra("project_name");
        project_description = getIntent().getStringExtra("project_description");
        TextView name = findViewById(R.id.name);
        name.setText(name1);
        TextView description = findViewById(R.id.description);
        description.setText(project_description);

        // Grab all the summary widgets we will keep updating as the range changes.
        currentPriceView = findViewById(R.id.currentStockPrice);
        percentageView = findViewById(R.id.percentage);
        rangeLabelView = findViewById(R.id.rangeLabel);
        trendArrowView = findViewById(R.id.imageView26);

        ImageButton exitButton = findViewById(R.id.backbutton234);
        exitButton.setOnClickListener(view -> {
            Intent b = new Intent(Analytics.this, PersonalPage.class);
            startActivity(b);
        });

        // Build the spark graph for this project and immediately draw the latest range.
        sparkView = findViewById(R.id.sparkview);

        buildSparkGraph(name1);
        setupRangeButtons();
    }

    void buildSparkGraph(String projectName) {
        // Make sure all simulated prices are generated up to this moment.
        BrownianStockManager.updateForHabit(this, projectName);
        ArrayList<StockDataPoint> history = BrownianStockManager.getHistoryForHabit(this, projectName);

        fullHistory.clear();
        if (!history.isEmpty()) {
            // Keep the history sorted so we can search by timestamp quickly.
            Collections.sort(history, Comparator.comparingLong(StockDataPoint::getTimestamp));
            fullHistory.addAll(history);
        }

        updateSparkForRange(currentRange);
    }

    private void setupRangeButtons() {
        RadioGroup rangeGroup = findViewById(R.id.rangeGroup);
        if (rangeGroup == null) {
            return;
        }

        // When the user taps a new period button, refresh the chart and summary text.
        rangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Range selected = mapRangeFromId(checkedId);
            if (selected != null && selected != currentRange) {
                currentRange = selected;
                updateSparkForRange(currentRange);
            }
        });

        // Default to showing the most recent day.
        rangeGroup.check(R.id.range24h);
    }

    private Range mapRangeFromId(int checkedId) {
        if (checkedId == R.id.range24h) {
            return Range.DAY_1;
        } else if (checkedId == R.id.range3d) {
            return Range.DAY_3;
        } else if (checkedId == R.id.range1w) {
            return Range.WEEK_1;
        } else if (checkedId == R.id.range1m) {
            return Range.MONTH_1;
        } else if (checkedId == R.id.range3m) {
            return Range.MONTH_3;
        } else if (checkedId == R.id.range1y) {
            return Range.YEAR_1;
        } else if (checkedId == R.id.rangeAll) {
            return Range.ALL;
        }
        return null;
    }

    private void updateSparkForRange(Range range) {
        if (sparkView == null) {
            return;
        }
        ArrayList<StockDataPoint> display = filterHistoryForRange(range);
        sparkView.setAdapter(new StockSparkAdapter(display));
        updateSummaryViews(display, range);
    }

    // Pull out only the points needed for the selected time window while keeping their order.
    private ArrayList<StockDataPoint> filterHistoryForRange(Range range) {
        if (fullHistory.isEmpty()) {
            return new ArrayList<>();
        }

        if (range == Range.ALL) {
            return new ArrayList<>(fullHistory);
        }

        long windowMillis = range.getWindowMillis();
        if (windowMillis <= 0L) {
            return new ArrayList<>(fullHistory);
        }

        StockDataPoint latest = fullHistory.get(fullHistory.size() - 1);
        long cutoff = latest.getTimestamp() - windowMillis;

        ArrayList<StockDataPoint> filtered = new ArrayList<>();
        for (StockDataPoint point : fullHistory) {
            if (point.getTimestamp() >= cutoff) {
                filtered.add(point);
            }
        }
        if (filtered.isEmpty()) {
            filtered.add(fullHistory.get(fullHistory.size() - 1));
        }
        return filtered;
    }

    // Update the large number, percent change, and arrow next to the chart.
    private void updateSummaryViews(ArrayList<StockDataPoint> display, Range range) {
        if (currentPriceView == null || percentageView == null || rangeLabelView == null || trendArrowView == null) {
            return;
        }

        if (display == null || display.isEmpty()) {
            // Nothing to chart yet, so reset the labels to their defaults.
            currentPriceView.setText(currencyFormat.format(0d));
            percentageView.setText(String.format(Locale.getDefault(), "%+.2f%%", 0f));
            rangeLabelView.setText(String.format(Locale.getDefault(), "VS %s", range.getLabel()));
            trendArrowView.setImageResource(R.drawable.os5_uparrow1);
            return;
        }

        StockDataPoint latest = display.get(display.size() - 1);
        double latestPrice = latest.getPrice();
        currentPriceView.setText(currencyFormat.format(latestPrice));

        StockDataPoint referencePoint = findReferencePoint(range, latest);
        double referencePrice = referencePoint != null ? referencePoint.getPrice() : latestPrice;
        double percentChange = referencePrice == 0d ? 0d : ((latestPrice - referencePrice) / referencePrice) * 100d;

        percentageView.setText(String.format(Locale.getDefault(), "%+.2f%%", percentChange));
        rangeLabelView.setText(String.format(Locale.getDefault(), "VS %s", range.getLabel()));
        trendArrowView.setImageResource(percentChange < 0d ? R.drawable.os5_downarrow2 : R.drawable.os5_uparrow1);
    }

    // Find the historical point we should compare the latest price against for the chosen range.
    private StockDataPoint findReferencePoint(Range range, StockDataPoint latest) {
        if (fullHistory.isEmpty() || latest == null) {
            return null;
        }

        if (range == Range.ALL) {
            // Compare to the very first recorded value when "All" is active.
            return fullHistory.get(0);
        }

        long windowMillis = range.getWindowMillis();
        if (windowMillis <= 0L) {
            return fullHistory.get(0);
        }

        long target = latest.getTimestamp() - windowMillis;
        StockDataPoint before = null;
        for (StockDataPoint point : fullHistory) {
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
        return before != null ? before : fullHistory.get(0);
    }

    private enum Range {
        DAY_1("24H", 24L * 60L * 60L * 1000L),
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

    // Simple adapter that feeds the spark view with our price history.
    public class StockSparkAdapter extends SparkAdapter {
        private final ArrayList<StockDataPoint> data;
        private final long baseTimestamp;

        public StockSparkAdapter(ArrayList<StockDataPoint> data) {
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
package com.example.iaso;

import android.graphics.Color;
import android.graphics.drawable.Drawable; // Import for Drawable
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import for ContextCompat

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart; // Import for LineChart
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry; // Import for Entry
import com.github.mikephil.charting.data.LineData; // Import for LineData
import com.github.mikephil.charting.data.LineDataSet; // Import for LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class AnalyticsActivity extends AppCompatActivity {

    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Initialize the chart
        initializeChart();

        // Setup sample data for BarChart
        setupChart();

        // Setup the yellow gradient line chart
        setupYellowGradientLineChart();
    }

    private void initializeChart() {
        barChart = findViewById(R.id.barChart);
        // If  declared yellowLineChart as a member,  initialize it here:
        // yellowLineChart = findViewById(R.id.yellowLineChart);
    }

    private void setupChart() {
        // Create sample data
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 1200f)); // Jan - Active Users
        entries.add(new BarEntry(1f, 1500f)); // Feb
        entries.add(new BarEntry(2f, 1800f)); // Mar
        entries.add(new BarEntry(3f, 2100f)); // Apr
        entries.add(new BarEntry(4f, 2400f)); // May
        entries.add(new BarEntry(5f, 2800f)); // Jun

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Active Users");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        // Create data object
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        // Set data to chart
        barChart.setData(barData);

        // Customize chart
        Description description = new Description();
        description.setText("Iaso Monthly Active Users");
        description.setTextSize(14f);
        barChart.setDescription(description);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Customize Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Refresh chart
        barChart.invalidate();
    }

    //  AnalyticsActivity.java for Graph #2
    private void setupYellowGradientLineChart() {
        // Find the LineChart view (add this to your layout first)
        LineChart lineChart = findViewById(R.id.yellowLineChart);

        // Sample data for the yellow gradient chart
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 20f));
        entries.add(new Entry(1f, 25f));
        entries.add(new Entry(2f, 30f));
        entries.add(new Entry(3f, 28f));
        entries.add(new Entry(4f, 35f));
        entries.add(new Entry(5f, 40f));
        entries.add(new Entry(6f, 45f));
        entries.add(new Entry(7f, 38f));
        entries.add(new Entry(8f, 42f));
        entries.add(new Entry(9f, 48f));

        // Create LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, "Progress");

        // Style the line - Yellow color like in Figma
        dataSet.setColor(Color.parseColor("#FFD700")); // Golden yellow
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false); // No dots on line
        dataSet.setDrawValues(false); // No value labels


        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.yellow_gradient);
        dataSet.setFillDrawable(drawable);
        dataSet.setDrawFilled(true);

        // Create LineData
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize chart appearance to match Figma
        customizeYellowChart(lineChart);

        // Refresh chart
        lineChart.invalidate();
    }

    private void customizeYellowChart(LineChart chart) {
        // Remove description
        chart.getDescription().setEnabled(false);

        // Remove legend
        chart.getLegend().setEnabled(false);

        // Customize X-axis to match Figma time labels
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"1D", "1M", "3M", "6M", "1Y", "2Y"}));

        // Customize Y-axis (left)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.parseColor("#666666"));
        leftAxis.setTextSize(10f);

        // Hide right Y-axis
        chart.getAxisRight().setEnabled(false);

        // Remove background and border
        chart.setDrawBorders(false);
        chart.setBackgroundColor(Color.WHITE);

        // Disable touch interactions for clean look
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        // Add some padding
        chart.setExtraOffsets(10f, 10f, 10f, 10f);
    }
}
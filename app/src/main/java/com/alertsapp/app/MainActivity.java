package com.alertsapp.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layoutSummary;
    private LinearLayout layoutStats;
    private LinearLayout layoutLegend;
    private EditText etCity, etFromDate, etToDate;
    private RadioGroup rgMode;
    private LinearLayout layoutCustomDates;
    private Button btnSearch;
    private ProgressBar progressBar;
    private TextView tvSummary, tvNoData;
    private BarChart barChart;
    private RecyclerView recyclerView;
    private TabHost tabHost;

    private AlertsApiClient apiClient;
    private AlertAdapter alertAdapter;
    private List<Alert> currentAlerts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new AlertsApiClient();

        bindViews();
        setupTabs();
        setupRecyclerView();
        setupModeSelector();
    }

    private void bindViews() {
        etCity = findViewById(R.id.etCity);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        rgMode = findViewById(R.id.rgMode);
        layoutCustomDates = findViewById(R.id.layoutCustomDates);
        btnSearch = findViewById(R.id.btnSearch);
        progressBar = findViewById(R.id.progressBar);
        tvSummary = findViewById(R.id.tvSummary);
        tvNoData = findViewById(R.id.tvNoData);
        barChart = findViewById(R.id.barChart);
        recyclerView = findViewById(R.id.tabAlerts);
        tabHost = findViewById(R.id.tabHost);
        btnSearch.setOnClickListener(v -> fetchAlerts());
        layoutSummary = findViewById(R.id.layoutSummary);
        layoutStats = findViewById(R.id.layoutStats);
        layoutLegend = findViewById(R.id.layoutLegend);
    }

    private void setupTabs() {
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("analysis");
        tab1.setIndicator("ניתוח");
        tab1.setContent(R.id.tabAnalysis);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("alerts");
        tab2.setIndicator("התראות");
        tab2.setContent(R.id.tabAlerts);
        tabHost.addTab(tab2);
    }

    private void setupRecyclerView() {
        alertAdapter = new AlertAdapter(currentAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(alertAdapter);
    }

    private void setupModeSelector() {
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            layoutCustomDates.setVisibility(checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE);
        });
    }

    private int getSelectedMode() {
        int id = rgMode.getCheckedRadioButtonId();
        if (id == R.id.rbLastDay) return 0;
        if (id == R.id.rbLastWeek) return 1;
        if (id == R.id.rbLastMonth) return 2;
        if (id == R.id.rbCustom) return 3;
        return 0;
    }

    private void fetchAlerts() {
        String city = etCity.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, "יש להזין שם עיר", Toast.LENGTH_SHORT).show();
            return;
        }

        int mode = getSelectedMode();
        String fromDate = etFromDate.getText().toString().trim();
        String toDate = etToDate.getText().toString().trim();

        if (mode == 3 && (fromDate.isEmpty() || toDate.isEmpty())) {
            Toast.makeText(this, "יש להזין תאריכי התחלה וסיום", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        tvNoData.setVisibility(View.GONE);

        apiClient.fetchAlerts(city, mode, fromDate, toDate, new AlertsApiClient.AlertsCallback() {
            @Override
            public void onSuccess(List<Alert> alerts) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentAlerts = alerts;

                    if (alerts.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        tvSummary.setText("לא נמצאו התראות עבור " + city);
                        barChart.setVisibility(View.GONE);
                        alertAdapter.setAlerts(new ArrayList<>());
                        return;
                    }

                    alerts.sort((a, b) -> {
                        String da = a.getAlertDate() != null ? a.getAlertDate() : "";
                        String db = b.getAlertDate() != null ? b.getAlertDate() : "";
                        int cmp = db.compareTo(da);
                        if (cmp != 0) return cmp;
                        String ta = a.getTime() != null ? a.getTime() : "";
                        String tb = b.getTime() != null ? b.getTime() : "";
                        return tb.compareTo(ta);
                    });

                    alertAdapter.setAlerts(alerts);
                    layoutSummary.setVisibility(View.VISIBLE);
                    layoutStats.setVisibility(View.VISIBLE);
                    layoutLegend.setVisibility(View.VISIBLE);
                    AlertAnalyzer.AnalysisResult result = AlertAnalyzer.analyze(alerts);
                    tvSummary.setText(result.riskSummary);
                    updateChart(result);
                    barChart.setVisibility(View.VISIBLE);

                    List<EventDuration> durations = AlertAnalyzer.calculateDurations(alerts);
                    Map<String, List<Long>> durationMap = new HashMap<>();

                    for (EventDuration d : durations) {
                        durationMap.putIfAbsent(d.getCategory(), new ArrayList<>());
                        durationMap.get(d.getCategory()).add(d.getDurationMinutes());
                    }

                    StringBuilder avgStayText = new StringBuilder("\nזמן משוער לצאת מהממד:\n");
                    for (Map.Entry<String, List<Long>> entry : durationMap.entrySet()) {
                        List<Long> times = entry.getValue();
                        long sum = 0;
                        for (Long t : times) sum += t;
                        long avg = times.isEmpty() ? 0 : sum / times.size();

                        Alert lastAlert = null;
                        for (int i = alerts.size() - 1; i >= 0; i--) {
                            Alert a = alerts.get(i);
                            if (a.getBaseCategory().equals(entry.getKey()) && a.getAlertType() == Alert.AlertType.DANGER) {
                                lastAlert = a;
                                break;
                            }
                        }

                        if (lastAlert != null) {

                            avgStayText.append(entry.getKey())
                                    .append(": זמן שהות ממוצע ")
                                    .append(avg)
                                    .append(" דקות");

                            avgStayText.append(" (מבוסס על ")
                                    .append(times.size())
                                    .append(" אירועים)");

                            avgStayText.append("\n\n");
                        }


                    }
                    tvSummary.setText(tvSummary.getText() + avgStayText.toString());
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateChart(AlertAnalyzer.AnalysisResult result) {
        List<BarEntry> entries = new ArrayList<>();
        Set<Integer> dangerHours = new HashSet<>();
        for (AlertAnalyzer.HourStat s : result.top5DangerHours) {
            if (s.count > 0) dangerHours.add(s.hour);
        }

        List<Integer> colors = new ArrayList<>();
        for (AlertAnalyzer.HourStat stat : result.hourStats) {
            entries.add(new BarEntry(stat.hour, stat.count));
            if (dangerHours.contains(stat.hour) && stat.count > 0) {
                colors.add(Color.rgb(220, 50, 50));
            } else if (stat.count == 0) {
                colors.add(Color.rgb(100, 200, 100));
            } else {
                colors.add(Color.rgb(255, 165, 0));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "התראות לפי שעה");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%02d", (int) value);
            }
        });
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(10f);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setBackgroundColor(Color.rgb(20, 20, 35));
        barChart.setDrawGridBackground(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        xAxis.setDrawGridLines(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!show);
    }
}

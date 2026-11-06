package com.example.invitegen;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private String dynamicCsvUrl;
    private String currentEventName;

    private PieChart pieChart;
    private TextView tvTotal;
    private ProgressBar syncProgress;
    private MaterialButton btnRefresh;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dynamicCsvUrl = getIntent().getStringExtra("EXTRA_CSV_URL");
        currentEventName = getIntent().getStringExtra("EXTRA_EVENT_NAME");

        pieChart = findViewById(R.id.pieChart);
        tvTotal = findViewById(R.id.tvTotal);
        syncProgress = findViewById(R.id.syncProgress);
        btnRefresh = findViewById(R.id.btnRefresh);

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> syncDataFromGoogleSheets());
        }

        syncDataFromGoogleSheets();
    }

    private void syncDataFromGoogleSheets() {
        if (dynamicCsvUrl == null || dynamicCsvUrl.isEmpty()) {
            loadChartData();
            return;
        }

        String syncUrl = dynamicCsvUrl;
        if (syncUrl.contains("/edit")) {
            syncUrl = syncUrl.split("/edit")[0] + "/export?format=csv";
        } else if (syncUrl.contains("/pubhtml")) {
            syncUrl = syncUrl.replace("/pubhtml", "/pub?output=csv");
        }

        if (syncProgress != null) syncProgress.setVisibility(View.VISIBLE);

        final String finalUrl = syncUrl;
        executorService.execute(() -> {
            Request request = new Request.Builder().url(finalUrl).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String csvData = response.body().string();
                    processCsvData(csvData);
                }
            } catch (IOException e) {
                Log.e("InviteGenSync", "Sync failed", e);
            }

            runOnUiThread(() -> {
                if (syncProgress != null) syncProgress.setVisibility(View.GONE);
                loadChartData();
            });
        });
    }

    private void processCsvData(String csv) {
        String[] lines = csv.split("\\r?\\n");
        AppDatabase db = AppDatabase.getInstance(this);
        db.guestDao().deleteAllGuests();

        int count = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            if (columns.length >= 3) {
                String guestName = columns[1].replace("\"", "").trim();
                String rsvpStatus = columns[2].replace("\"", "").trim();
                
                db.guestDao().insert(new Guest(guestName, rsvpStatus, currentEventName));
                count++;
            }
        }
        
        final int syncedCount = count;
        runOnUiThread(() -> Toast.makeText(this, "Synced " + syncedCount + " responses", Toast.LENGTH_SHORT).show());
    }

    private void loadChartData() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            
            // USE PATTERN MATCHING TO CATCH ALL 21 RESPONSES
            // Looks for anything starting with "yes", "no", or "maybe"
            int yesCount = db.guestDao().getCountByPattern("yes%");
            int noCount = db.guestDao().getCountByPattern("no%");
            int maybeCount = db.guestDao().getCountByPattern("maybe%");
            
            int total = yesCount + noCount + maybeCount;

            runOnUiThread(() -> {
                tvTotal.setText("Total Responses: " + total);
                
                ArrayList<PieEntry> entries = new ArrayList<>();
                if (yesCount > 0) entries.add(new PieEntry(yesCount, "Yes"));
                if (noCount > 0) entries.add(new PieEntry(noCount, "No"));
                if (maybeCount > 0) entries.add(new PieEntry(maybeCount, "Maybe"));

                if (entries.isEmpty()) {
                    pieChart.clear();
                    pieChart.setNoDataText("No matching data found. Check your Form text!");
                    return;
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextColor(Color.BLACK);
                dataSet.setValueTextSize(14f);

                PieData data = new PieData(dataSet);
                pieChart.setData(data);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText(currentEventName != null ? currentEventName : "Analytics");
                pieChart.setCenterTextSize(16f);
                pieChart.animateY(1000);
                pieChart.invalidate();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

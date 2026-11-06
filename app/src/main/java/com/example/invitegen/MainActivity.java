package com.example.invitegen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    private Button btnBirthday, btnWedding, btnEngagement, btnCustom, btnDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install Splash Screen before super.onCreate
        SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Standard Buttons
        btnBirthday = findViewById(R.id.btnBirthday);
        btnWedding = findViewById(R.id.btnWedding);
        btnEngagement = findViewById(R.id.btnEngagement);
        btnCustom = findViewById(R.id.btnCustom);
        btnDashboard = findViewById(R.id.btnDashboard);

        // Event Type Listeners
        btnBirthday.setOnClickListener(v -> openDetails("Birthday"));
        btnWedding.setOnClickListener(v -> openDetails("Wedding"));
        btnEngagement.setOnClickListener(v -> openDetails("Engagement"));
        btnCustom.setOnClickListener(v -> openDetails("Custom"));
        
        btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        });
    }

    private void openDetails(String eventType) {
        Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
        intent.putExtra("eventType", eventType);
        startActivity(intent);
    }
}

package com.example.myapplication;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;



import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // UI components
    TextView percentageText, dateText;
    ImageView dropletIcon, refreshIcon, settingsIcon, logoImage;
    Switch themeToggle;

    // Handler for timed tasks
    Handler handler = new Handler();

    // Moisture threshold set by user
    int userThreshold = 50;

    // Tracks if user manually refreshed to pause auto-refresh briefly
    boolean isManualRefresh = false;

    // URL of the Google Apps Script that provides moisture data
    String sheetUrl = "https://script.google.com/macros/s/AKfycbzO9c6yoWUkvONNBWzGil9Cu0tQEl559JTXyAd1RAll4UoBF-8yxhbKxT1XbFj5Ydk/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind all UI views from XML
        bindViews();

        // Apply dark/light theme and update icons accordingly
        setupTheme();

        // Create notification channel for alerting users
        NotificationManager.createChannel(this);

        // Ask for notification permission (for Android 13+)
        handlePermissions();

        // Load previously saved moisture threshold
        setupThreshold();

        // Fetch latest data from the sheet and display it
        fetchData();

        // Enable periodic refresh every 30 minutes
        startAutoRefresh();

        // Setup refresh and settings button behavior
        setupListeners();
    }

    // Connect variables to their respective views in the XML layout
    private void bindViews() {
        percentageText = findViewById(R.id.percentageText);
        dateText = findViewById(R.id.dateText);
        dropletIcon = findViewById(R.id.dropletIcon);
        refreshIcon = findViewById(R.id.refreshIcon);
        settingsIcon = findViewById(R.id.settingsIcon);
        logoImage = findViewById(R.id.logoImage);
        themeToggle = findViewById(R.id.themeToggle);
    }

    // Load and apply the current theme, and update icons for it
    private void setupTheme() {
        boolean isDark = ThemeManager.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        themeToggle.setChecked(isDark);
        updateIconsForTheme(isDark);

        themeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(this, isChecked);
            updateIconsForTheme(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // Request POST_NOTIFICATIONS permission if required
    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != getPackageManager().PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    // Load saved user-defined moisture threshold from shared preferences
    private void setupThreshold() {
        SharedPreferences prefs = getSharedPreferences("SmartPlantPrefs", MODE_PRIVATE);
        userThreshold = prefs.getInt("moistureThreshold", 50);
    }

    // Fetch the most recent moisture value from the sheet
    private void fetchData() {
        DataFetcher.fetchLatestData(sheetUrl, handler, new DataFetcher.FetchCallback() {
            @Override
            public void onResult(String timestamp, String moisture) {
                String finalMoisture = moisture + "%";
                String finalTime = getCurrentTime();

                // Update UI
                percentageText.setText(finalMoisture);
                dateText.setText("Last updated:\n" + finalTime);

                int moistureLevel = Integer.parseInt(moisture);
                updateDropletIcon(moistureLevel);

                // Trigger alert if value is below user-defined threshold
                if (moistureLevel < userThreshold) {
                    NotificationManager.sendLowMoistureNotification(MainActivity.this, moistureLevel);
                }
            }

            @Override
            public void onError(String error) {
                percentageText.setText("Error");
                Log.e("MainActivity", "Error fetching data: " + error);
            }
        });
    }

    // Set up click listeners for refresh and settings icons
    private void setupListeners() {
        refreshIcon.setOnClickListener(v -> {
            boolean isDark = ThemeManager.isDarkMode(this);
            refreshIcon.setImageResource(R.drawable.ic_refresh_dark); // Temporary visual on tap

            fetchData(); // Trigger manual fetch
            isManualRefresh = true;

            handler.postDelayed(() -> {
                refreshIcon.setImageResource(isDark ? R.drawable.refresh_d_mode : R.drawable.ic_refresh);
                isManualRefresh = false;
            }, 200);
        });

        settingsIcon.setOnClickListener(v -> {
            boolean isDark = ThemeManager.isDarkMode(this);
            settingsIcon.setImageResource(R.drawable.ic_settings_dark);

            handler.postDelayed(() -> settingsIcon.setImageResource(isDark ? R.drawable.settings_d_mode : R.drawable.ic_settings), 200);

            // Create a dialog for user input
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Set Moisture Notification Threshold");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            input.setHint("Enter value from 0 to 100");
            builder.setView(input);

            // Save new value on confirm
            builder.setPositiveButton("Set", (dialog, which) -> {
                String inputValue = input.getText().toString().trim();
                if (!inputValue.isEmpty()) {
                    try {
                        int value = Integer.parseInt(inputValue);
                        if (value >= 0 && value <= 100) {
                            userThreshold = value;
                            SharedPreferences.Editor editor = getSharedPreferences("SmartPlantPrefs", MODE_PRIVATE).edit();
                            editor.putInt("moistureThreshold", value);
                            editor.apply();
                        } else {
                            Toast.makeText(MainActivity.this, "Value must be between 0 and 100", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    // Dynamically update icons to match current theme
    private void updateIconsForTheme(boolean isDarkMode) {
        logoImage.setImageResource(isDarkMode ? R.drawable.logo_d_mode : R.drawable.logo);
        refreshIcon.setImageResource(isDarkMode ? R.drawable.refresh_d_mode : R.drawable.ic_refresh);
        settingsIcon.setImageResource(isDarkMode ? R.drawable.settings_d_mode : R.drawable.ic_settings);
    }

    // Return current time in formatted string
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date());
    }

    // Refresh data every 30 minutes automatically
    private void startAutoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isManualRefresh) fetchData();
                handler.postDelayed(this, 1800000); // 30 min
            }
        }, 1800000);
    }

    // Change droplet icon based on current moisture level
    private void updateDropletIcon(int moisturePercentage) {
        if (moisturePercentage >= 98) dropletIcon.setImageResource(R.drawable.d_100);
        else if (moisturePercentage >= 85) dropletIcon.setImageResource(R.drawable.d_85);
        else if (moisturePercentage >= 75) dropletIcon.setImageResource(R.drawable.d_75);
        else if (moisturePercentage >= 55) dropletIcon.setImageResource(R.drawable.d_55);
        else if (moisturePercentage >= 45) dropletIcon.setImageResource(R.drawable.d_45);
        else if (moisturePercentage >= 25) dropletIcon.setImageResource(R.drawable.d_25);
        else if (moisturePercentage >= 1) dropletIcon.setImageResource(R.drawable.d_1);
        else dropletIcon.setImageResource(R.drawable.d_0);
    }
}
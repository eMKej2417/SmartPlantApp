package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

/**
 * Handles creation of notification channel and sending notifications related to soil moisture.
 */
public class NotificationManager {

    // Unique ID for the notification channel
    private static final String CHANNEL_ID = "plant_monitor_channel";

    /**
     * Creates a notification channel for Android O and above.
     * This is required for notifications to work properly.
     *
     * @param context The application context used to access system services
     */
    public static void createChannel(Context context) {
        // Only create channel on Android O (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Moisture Alert", // Channel name shown in system settings
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notification channel for moisture level alerts");

            // Get system notification service and register the channel
            android.app.NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Sends a low moisture level alert notification to the user.
     *
     * @param context The application context
     * @param level The current moisture level to include in the notification
     */
    public static void sendLowMoistureNotification(Context context, int level) {
        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning) // Your alert icon in drawable
                .setContentTitle("Moisture Alert!")  // Notification title
                .setContentText("Moisture level is low: " + level + "%") // Message with current level
                .setPriority(Notification.PRIORITY_DEFAULT); // Default priority for visibility

        // Show the notification using the system service
        android.app.NotificationManager manager = (android.app.NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(0, builder.build()); // ID 0 is fine if sending one notification type
        }
    }
}

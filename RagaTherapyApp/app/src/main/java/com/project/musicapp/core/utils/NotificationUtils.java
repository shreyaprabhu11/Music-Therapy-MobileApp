package com.project.musicapp.core.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.project.musicapp.R;
import com.project.musicapp.features.patient.activities.NotificationActivity;
import com.project.musicapp.features.patient.activities.NotificationDetailActivity;

public class NotificationUtils {

    private static final String CHANNEL_ID = "appointment_channel";
    private static final String CHANNEL_NAME = "Appointment Notifications";

    /**
     * Show a pop-up notification that opens a specified activity when clicked.
     *
     * @param context Context
     * @param title   Notification title
     * @param message Notification message
     */
    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for appointments");
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent to open NotificationDetailActivity
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your app icon
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent); // attach intent

        // Show notification
        int notificationId = (int) System.currentTimeMillis(); // unique ID
        notificationManager.notify(notificationId, builder.build());
    }
}

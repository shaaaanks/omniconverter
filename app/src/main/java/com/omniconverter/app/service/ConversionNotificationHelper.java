package com.omniconverter.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.omniconverter.app.MainActivity;
import com.omniconverter.app.R;

public class ConversionNotificationHelper {
    private static final String CONVERSION_CHANNEL_ID = "conversion_complete_channel";
    private static final String CONVERSION_CHANNEL_NAME = "Conversion Complete";
    private static final int NOTIFICATION_ID = 0xF002;

    public static void showConversionComplete(Context context, String fileName, String format) {
        createChannelIfNeeded(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CONVERSION_CHANNEL_ID)
                .setContentTitle("Conversion Completed")
                .setContentText(fileName + " → " + format)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setColor(Color.parseColor("#235347"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    public static void showConversionFailed(Context context, String fileName, String errorMessage) {
        createChannelIfNeeded(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CONVERSION_CHANNEL_ID)
                .setContentTitle("Conversion Failed")
                .setContentText(fileName + ": " + errorMessage)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setColor(Color.parseColor("#FF5252"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID + 1, builder.build());
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CONVERSION_CHANNEL_ID, CONVERSION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for file conversion completion");
            nm.createNotificationChannel(channel);
        }
    }
}

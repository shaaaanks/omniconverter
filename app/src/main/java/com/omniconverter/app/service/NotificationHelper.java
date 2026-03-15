package com.omniconverter.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "omniconverter_channel";
    private static final String CHANNEL_NAME = "OmniConverter";

    public static Notification buildNotification(Context context, String title, String text, int progress, boolean indeterminate) {
        createChannelIfNeeded(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setColor(Color.parseColor("#235347"));

        if (indeterminate) builder.setProgress(0, 0, true);
        else builder.setProgress(100, progress, false);

        return builder.build();
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("OmniConverter background tasks");
            nm.createNotificationChannel(channel);
        }
    }
}

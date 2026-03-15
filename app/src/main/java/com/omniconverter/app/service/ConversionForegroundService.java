package com.omniconverter.app.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.omniconverter.app.service.NotificationHelper;

public class ConversionForegroundService extends Service {

    public static final int NOTIFICATION_ID = 0xF01D;

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = NotificationHelper.buildNotification(this,
                "OmniConverter", "Conversion running", 0, false);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service will continue running until explicitly stopped.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

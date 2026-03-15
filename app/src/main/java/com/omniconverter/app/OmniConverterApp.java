package com.omniconverter.app;

import android.app.Application;
import android.util.Log;

import com.omniconverter.app.storage.ConversionDatabase;
import com.omniconverter.app.storage.DatabaseTestHelper;

public class OmniConverterApp extends Application {
    private static final String TAG = "OmniConverterApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application starting...");

        // Initialize the database on app startup
        ConversionDatabase.getDatabase(this);
        Log.d(TAG, "Database initialized");

        // Optionally test database operations (remove in production)
        DatabaseTestHelper.testDatabase(this);
    }
}



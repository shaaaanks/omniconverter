package com.omniconverter.app.storage;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class DatabaseTestHelper {
    private static final String TAG = "DatabaseTest";

    /**
     * Test basic database operations
     */
    public static void testDatabase(Context context) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance(context);

            // Create test record
            Log.d(TAG, "Creating test conversion record...");
            ConversionEntity testEntity = new ConversionEntity(
                    "content://test/input.jpg",
                    "file:///storage/emulated/0/OmniConverter/output.png",
                    "IMAGE",
                    "SUCCESS",
                    "Test conversion",
                    System.currentTimeMillis()
            );
            long insertedId = dbManager.insertConversion(testEntity);
            Log.d(TAG, "Test record inserted with ID: " + insertedId);

            // Retrieve all records
            Log.d(TAG, "Retrieving all records...");
            List<ConversionEntity> allRecords = dbManager.getAllConversions();
            Log.d(TAG, "Total records in database: " + allRecords.size());

            // Log each record
            for (ConversionEntity record : allRecords) {
                Log.d(TAG, "Record: ID=" + record.id + ", Type=" + record.type +
                      ", Status=" + record.status + ", Timestamp=" + record.timestamp);
            }

            // Get count
            int count = dbManager.getTotalCount();
            Log.d(TAG, "Database count: " + count);

            Log.d(TAG, "✓ Database test completed successfully!");

        } catch (Exception e) {
            Log.e(TAG, "✗ Database test failed: " + e.getMessage(), e);
        }
    }

    /**
     * Clear all records (for testing)
     */
    public static void clearAllRecords(Context context) {
        try {
            DatabaseManager.getInstance(context).deleteAllConversions();
            Log.d(TAG, "✓ All records cleared");
        } catch (Exception e) {
            Log.e(TAG, "✗ Error clearing records: " + e.getMessage(), e);
        }
    }
}

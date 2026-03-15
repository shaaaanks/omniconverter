package com.omniconverter.app.storage;

import android.content.Context;

import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private ConversionDatabase db;

    private DatabaseManager(Context context) {
        this.db = ConversionDatabase.getDatabase(context);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    // Insert a conversion record
    public long insertConversion(ConversionEntity entity) {
        return db.conversionDao().insert(entity);
    }

    // Update a conversion record
    public void updateConversion(ConversionEntity entity) {
        new Thread(() -> db.conversionDao().update(entity)).start();
    }

    // Delete a conversion record
    public void deleteConversion(ConversionEntity entity) {
        new Thread(() -> db.conversionDao().delete(entity)).start();
    }

    // Get all conversions
    public List<ConversionEntity> getAllConversions() {
        return db.conversionDao().getAll();
    }

    // Get conversion by ID
    public ConversionEntity getConversionById(long id) {
        return db.conversionDao().getById(id);
    }

    // Get conversions by status
    public List<ConversionEntity> getConversionsByStatus(String status) {
        return db.conversionDao().getByStatus(status);
    }

    // Get conversions by type
    public List<ConversionEntity> getConversionsByType(String type) {
        return db.conversionDao().getByType(type);
    }

    // Delete all conversions
    public void deleteAllConversions() {
        new Thread(() -> db.conversionDao().deleteAll()).start();
    }

    // Get total count
    public int getTotalCount() {
        return db.conversionDao().getCount();
    }
}

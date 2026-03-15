package com.omniconverter.app.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ConversionEntity.class}, version = 1, exportSchema = false)
public abstract class ConversionDatabase extends RoomDatabase {
    public abstract ConversionDao conversionDao();

    private static volatile ConversionDatabase INSTANCE;

    public static ConversionDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ConversionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ConversionDatabase.class, "conversion_db").build();
                }
            }
        }
        return INSTANCE;
    }
}

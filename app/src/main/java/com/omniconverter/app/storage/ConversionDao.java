package com.omniconverter.app.storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ConversionDao {
    @Insert
    long insert(ConversionEntity entity);

    @Update
    void update(ConversionEntity entity);

    @Delete
    void delete(ConversionEntity entity);

    @Query("SELECT * FROM conversions ORDER BY timestamp DESC")
    List<ConversionEntity> getAll();

    @Query("SELECT * FROM conversions WHERE id = :id LIMIT 1")
    ConversionEntity getById(long id);

    @Query("SELECT * FROM conversions WHERE status = :status ORDER BY timestamp DESC")
    List<ConversionEntity> getByStatus(String status);

    @Query("SELECT * FROM conversions WHERE type = :type ORDER BY timestamp DESC")
    List<ConversionEntity> getByType(String type);

    @Query("DELETE FROM conversions")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM conversions")
    int getCount();
}

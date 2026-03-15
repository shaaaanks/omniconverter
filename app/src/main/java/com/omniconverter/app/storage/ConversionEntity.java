package com.omniconverter.app.storage;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversions")
public class ConversionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "input_uri")
    public String inputUri;

    @ColumnInfo(name = "output_uri")
    public String outputUri;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public ConversionEntity(String inputUri, String outputUri, String type, String status, String message, long timestamp) {
        this.inputUri = inputUri;
        this.outputUri = outputUri;
        this.type = type;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}

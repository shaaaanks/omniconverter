package com.omniconverter.app.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.omniconverter.app.converters.Converter;
import com.omniconverter.app.converters.ImageConverter;
import com.omniconverter.app.converters.VideoToAudioConverter;
import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.service.ConversionNotificationHelper;
import com.omniconverter.app.storage.ConversionEntity;
import com.omniconverter.app.storage.DatabaseManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConversionWorker extends Worker {

    public static final String KEY_URI = "input_uri";
    public static final String KEY_TYPE = "type";
    public static final String KEY_FORMAT = "format";

    public ConversionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uriStr = getInputData().getString(KEY_URI);
        String type = getInputData().getString(KEY_TYPE);
        String format = getInputData().getString(KEY_FORMAT);
        String[] additionalFiles = getInputData().getStringArray("additional_files");

        if (uriStr == null || type == null) return Result.failure();
        Uri uri = Uri.parse(uriStr);
        Converter converter = selectConverter(type);
        if (converter == null) return Result.failure();

        Map<String, Object> params = new HashMap<>();
        if (format != null) {
            params.put("format", format);
        }
        if (additionalFiles != null) {
            params.put("additional_files", additionalFiles);
        }

        String fileName = getFileNameFromUri(uri);
        ConversionResult result;
        try {
            result = converter.convert(getApplicationContext(), uri, params);
        } catch (Exception e) {
            result = new ConversionResult(uri);
            result.setStatus(ConversionResult.Status.FAILED);
            result.setMessage(e.getMessage());
        }

        // Persist to database using DatabaseManager (thread-safe)
        ConversionEntity entity = new ConversionEntity(
                result.getInputUri().toString(),
                result.getOutputUri() != null ? result.getOutputUri().toString() : null,
                type,
                result.getStatus().name(),
                result.getMessage(),
                System.currentTimeMillis()
        );
        DatabaseManager.getInstance(getApplicationContext()).insertConversion(entity);

        // Show notification
        if (result.getStatus() == ConversionResult.Status.SUCCESS) {
            format = (String) params.get("format");
            if (format == null) format = type;
            ConversionNotificationHelper.showConversionComplete(getApplicationContext(), fileName, format);
            Data outputData = new Data.Builder()
                .putString("outputUri", result.getOutputUri().toString())
                .build();
            return Result.success(outputData);
        } else {
            ConversionNotificationHelper.showConversionFailed(getApplicationContext(), fileName, result.getMessage());
            return Result.failure();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String uriStr = uri.toString();
        if (uriStr.contains("/")) {
            return uriStr.substring(uriStr.lastIndexOf("/") + 1);
        }
        return "file";
    }

    private Converter selectConverter(String type) {
        if ("IMAGE".equalsIgnoreCase(type)) return new ImageConverter();
        if ("MP4_TO_MP3".equalsIgnoreCase(type)) return new VideoToAudioConverter();
        if ("PDF_MERGE".equalsIgnoreCase(type)) return new com.omniconverter.app.converters.PDFMergerConverter();
        if ("DOCX_TO_PDF".equalsIgnoreCase(type)) return new com.omniconverter.app.converters.WordToPDFConverter();
        return null;
    }
}

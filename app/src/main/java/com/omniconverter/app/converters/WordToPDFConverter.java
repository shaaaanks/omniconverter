package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;
import com.omniconverter.app.network.ConvertApiClient;

import java.util.Map;

public class WordToPDFConverter implements Converter {
    private static final String TAG = "WordToPDF";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        Log.d(TAG, "Converting Word document to PDF");

        // Get file name for API
        String fileName = FileUtils.getFileDisplayName(context, inputUri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "document.docx";
        }

        try {
            // Try ConvertAPI fallback
            Log.d(TAG, "Using ConvertAPI fallback for Word to PDF conversion");
            result = ConvertApiClient.convertToPdf(context, inputUri, fileName);
            Log.d(TAG, "ConvertAPI conversion successful");
        } catch (Exception e) {
            Log.e(TAG, "ConvertAPI conversion failed: " + e.getMessage());
            throw new Exception("Word to PDF conversion failed: " + e.getMessage() +
                    ". Please check your ConvertAPI secret key and internet connection.");
        }

        return result;
    }
}

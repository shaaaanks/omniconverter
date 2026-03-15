package com.omniconverter.app.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.AppConfig;
import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ConvertApiClient {
    private static final String TAG = "ConvertApiClient";
    private static final String BASE_URL = AppConfig.CONVERT_API_BASE_URL;
    private static final String SECRET_KEY = AppConfig.CONVERT_API_SECRET;

    /**
     * Convert file using ConvertAPI as fallback
     */
    public static ConversionResult convertToPdf(Context context, Uri inputUri, String fileName) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        Log.d(TAG, "Starting ConvertAPI fallback conversion for: " + fileName);

        // Copy input file to temp location
        File inputFile = FileUtils.copyUriToFile(context, inputUri, "convertapi_input.tmp");

        // Upload file to ConvertAPI
        String fileId = uploadFile(inputFile, fileName);
        Log.d(TAG, "File uploaded, ID: " + fileId);

        // Convert to PDF
        String downloadUrl = convertFile(fileId, "pdf");
        Log.d(TAG, "Conversion initiated, download URL: " + downloadUrl);

        // Download converted file
        File outputFile = downloadConvertedFile(context, downloadUrl, fileName);
        Log.d(TAG, "File downloaded to: " + outputFile.getAbsolutePath());

        result.setOutputUri(Uri.fromFile(outputFile));
        result.setStatus(ConversionResult.Status.SUCCESS);
        return result;
    }

    /**
     * Upload file to ConvertAPI
     */
    private static String uploadFile(File file, String fileName) throws Exception {
        String boundary = "----ConvertApiBoundary" + System.currentTimeMillis();
        URL url = new URL(BASE_URL + "/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + SECRET_KEY);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            // Write boundary
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
            os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

            // Write file content
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Upload failed with code: " + responseCode);
        }

        // Parse response to get file ID
        try (InputStream is = conn.getInputStream()) {
            String response = readStream(is);
            // Parse JSON response to extract file ID
            // For simplicity, assuming response contains "FileId": "some_id"
            String fileId = extractFileIdFromResponse(response);
            return fileId;
        }
    }

    /**
     * Convert uploaded file to PDF
     */
    private static String convertFile(String fileId, String targetFormat) throws Exception {
        URL url = new URL(BASE_URL + "/convert/any/to/" + targetFormat + "?File=" + fileId + "&StoreFile=true");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + SECRET_KEY);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Conversion failed with code: " + responseCode);
        }

        // Parse response to get download URL
        try (InputStream is = conn.getInputStream()) {
            String response = readStream(is);
            return extractDownloadUrlFromResponse(response);
        }
    }

    /**
     * Download converted file
     */
    private static File downloadConvertedFile(Context context, String downloadUrl, String originalFileName) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        File outDir = context.getExternalFilesDir("PDFs");
        if (outDir == null) {
            outDir = new File(context.getCacheDir(), "PDFs");
        }
        if (!outDir.exists()) outDir.mkdirs();

        String baseName = originalFileName;
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
        File outputFile = new File(outDir, baseName + "_converted.pdf");

        try (InputStream is = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return outputFile;
    }

    /**
     * Helper methods
     */
    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead));
        }
        return sb.toString();
    }

    private static String extractFileIdFromResponse(String response) {
        // Simple JSON parsing - in production use proper JSON library
        int fileIdIndex = response.indexOf("\"FileId\":\"");
        if (fileIdIndex != -1) {
            int start = fileIdIndex + 10;
            int end = response.indexOf("\"", start);
            return response.substring(start, end);
        }
        return null;
    }

    private static String extractDownloadUrlFromResponse(String response) {
        // Simple JSON parsing - in production use proper JSON library
        int urlIndex = response.indexOf("\"Url\":\"");
        if (urlIndex != -1) {
            int start = urlIndex + 7;
            int end = response.indexOf("\"", start);
            return response.substring(start, end);
        }
        return null;
    }
}

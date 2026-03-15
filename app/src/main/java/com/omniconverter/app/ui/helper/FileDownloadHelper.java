package com.omniconverter.app.ui.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.omniconverter.app.core.FileUtils;

import java.io.File;

public class FileDownloadHelper {

    /**
     * Open a file using the default app
     */
    public static void openFile(Context context, String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);

            // If it's a file URI, convert to FileProvider URI
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share a file
     */
    public static void shareFile(Context context, String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);

            // If it's a file URI, convert to FileProvider URI
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share file"));
        } catch (Exception e) {
            Toast.makeText(context, "Cannot share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete a file
     */
    public static boolean deleteFile(String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if file exists
     */
    public static boolean fileExists(String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                return file.exists();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get file size
     */
    public static long getFileSize(String fileUri) {
        try {
            Uri uri = Uri.parse(fileUri);
            if ("file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                return file.length();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Save file to Downloads folder
     */
    public static void saveToDownloads(Context context, String fileUri, String fileName) {
        try {
            Uri uri = Uri.parse(fileUri);
            File sourceFile;

            if ("file".equals(uri.getScheme())) {
                sourceFile = new File(uri.getPath());
            } else if ("content".equals(uri.getScheme())) {
                sourceFile = FileUtils.copyUriToFile(context, uri, "temp_file");
            } else {
                Toast.makeText(context, "Cannot save file: invalid URI scheme", Toast.LENGTH_SHORT).show();
                return;
            }

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File omniConverterDir = new File(downloadsDir, "OmniConverter");
            if (!omniConverterDir.exists()) {
                omniConverterDir.mkdirs();
            }
            File destFile = new File(omniConverterDir, fileName);
            FileUtils.copyFile(sourceFile, destFile);
            Toast.makeText(context, "File saved to Downloads/OmniConverter: " + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

package com.omniconverter.app.core;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@SuppressWarnings("unused")
public class FileUtils {

    public static File getAppStorageDir(Context context, String subfolder) {
        File base = new File(context.getExternalFilesDir(null), AppConfig.APP_FOLDER);
        File dir = new File(base, subfolder);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public static File copyUriToFile(Context context, Uri uri, String outFileName) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new Exception("Unable to open input stream");
            File out = new File(context.getCacheDir(), outFileName);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
            }
            return out;
        }
    }

    public static String getFileDisplayName(Context context, Uri uri) {
        String name = "";
        try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return name;
    }

    public static void copyFile(File source, File dest) throws Exception {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
        }
    }
}

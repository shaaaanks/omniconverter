package com.omniconverter.app.converters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VideoToFramesConverter implements Converter {
    private static final String TAG = "VideoToFrames";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        Log.d(TAG, "Starting Video to Frames conversion (MediaMetadataRetriever)");
        ConversionResult result = new ConversionResult(inputUri);

        String format = (String) params.get("format");
        if (format == null) format = "jpg";
        format = format.toLowerCase();
        CompressFormat compressFormat = mapFormat(format);

        // Copy input video locally
        File inputFile = FileUtils.copyUriToFile(context, inputUri, "input_video_" + System.currentTimeMillis() + ".mp4");

        // Prepare output dir
        File framesDir = new File(context.getCacheDir(), "extracted_frames_" + System.currentTimeMillis());
        if (!framesDir.exists() && !framesDir.mkdirs()) {
            throw new Exception("Unable to create frames directory");
        }

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(inputFile.getAbsolutePath());
            String durMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = durMsStr != null ? Long.parseLong(durMsStr) : 0;
            if (durationMs <= 0) throw new Exception("Invalid video duration");

            // Extract 1 frame per second
            int frameIntervalMs = 1000;
            int frameIndex = 0;
            for (long t = 0; t < durationMs; t += frameIntervalMs) {
                Bitmap frame = retriever.getFrameAtTime(t * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                if (frame == null) continue;
                File out = new File(framesDir, String.format("frame_%03d.%s", frameIndex, format));
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    frame.compress(compressFormat, format.equals("png") ? 100 : 90, fos);
                }
                frame.recycle();
                frameIndex++;
            }

            // Zip frames
            File zipFile = new File(context.getExternalFilesDir("Documents"), "frames_" + System.currentTimeMillis() + ".zip");
            zipFiles(framesDir.listFiles(), zipFile);

            result.setOutputUri(Uri.fromFile(zipFile));
            result.setStatus(ConversionResult.Status.SUCCESS);
        } catch (Exception e) {
            Log.e(TAG, "Frame extraction failed: " + e.getMessage());
            result.setStatus(ConversionResult.Status.FAILED);
            result.setMessage(e.getMessage());
            throw e;
        } finally {
            if (inputFile.exists() && !inputFile.delete()) {
                Log.w(TAG, "Failed to delete input file: " + inputFile.getAbsolutePath());
            }
            deleteRecursive(framesDir);
        }

        return result;
    }

    private CompressFormat mapFormat(String format) {
        switch (format) {
            case "png":
                return CompressFormat.PNG;
            case "webp":
                return CompressFormat.WEBP;
            case "jpg":
            case "jpeg":
            default:
                return CompressFormat.JPEG;
        }
    }

    private void zipFiles(File[] files, File zipFile) throws Exception {
        if (files == null || files.length == 0) throw new Exception("No frames extracted");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[8192];
            for (File file : files) {
                if (file == null || file.isDirectory()) continue;
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null) return;
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!fileOrDirectory.delete()) {
            Log.w(TAG, "Failed to delete file: " + fileOrDirectory.getAbsolutePath());
        }
    }
}

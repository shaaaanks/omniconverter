package com.omniconverter.app.converters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class ImageConverter implements Converter {
    private static final String TAG = "ImageConverter";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);
        String format = "jpg";
        Object fobj = params.get("format");
        if (fobj != null) format = fobj.toString().toLowerCase();

        Log.d(TAG, "Converting image to format: " + format);

        // Copy input file to cache
        File tmp = FileUtils.copyUriToFile(context, inputUri, "img_input.tmp");
        Bitmap bmp = BitmapFactory.decodeFile(tmp.getAbsolutePath());
        if (bmp == null) throw new Exception("Unable to decode image");

        // Save to external app files directory (accessible by file manager)
        File outDir = context.getExternalFilesDir("Images");
        if (outDir == null) {
            outDir = new File(context.getCacheDir(), "Images");
        }
        if (!outDir.exists()) outDir.mkdirs();

        File out = new File(outDir, "converted_" + System.currentTimeMillis() + "." + format);

        Bitmap.CompressFormat cf = Bitmap.CompressFormat.JPEG;
        if ("png".equalsIgnoreCase(format)) cf = Bitmap.CompressFormat.PNG;
        else if ("webp".equalsIgnoreCase(format)) cf = Bitmap.CompressFormat.WEBP;
        else if ("bmp".equalsIgnoreCase(format)) {
            // BMP not directly supported, save as PNG and rename? But for simplicity, use PNG
            cf = Bitmap.CompressFormat.PNG;
            format = "png";
        }

        try (FileOutputStream fos = new FileOutputStream(out)) {
            bmp.compress(cf, 90, fos);
        }

        Log.d(TAG, "File saved to: " + out.getAbsolutePath());

        // Create proper URI for the file
        Uri outputUri;
        try {
            outputUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", out);
        } catch (Exception e) {
            Log.d(TAG, "FileProvider failed, using file URI: " + e.getMessage());
            outputUri = Uri.fromFile(out);
        }

        result.setOutputUri(outputUri);
        result.setStatus(ConversionResult.Status.SUCCESS);
        Log.d(TAG, "Conversion successful: " + out.getAbsolutePath());
        return result;
    }
}

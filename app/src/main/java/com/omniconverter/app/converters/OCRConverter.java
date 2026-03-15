package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OCRConverter implements Converter {
    private static final String TAG = "OCRConverter";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        Log.d(TAG, "Starting OCR conversion for: " + inputUri);
        ConversionResult result = new ConversionResult(inputUri);

        try {
            // 1. Prepare Input Image
            InputImage image = InputImage.fromFilePath(context, inputUri);

            // 2. Initialize Text Recognizer
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // 3. Perform Recognition (Synchronously using Tasks.await)
            Task<Text> task = recognizer.process(image);
            Text visionText = Tasks.await(task, 30, TimeUnit.SECONDS);

            String extractedText = visionText.getText();
            if (extractedText == null || extractedText.isEmpty()) {
                throw new Exception("No text detected in the image.");
            }

            // 4. Save to Text File
            File outDir = context.getExternalFilesDir("Documents");
            if (outDir == null) outDir = context.getCacheDir();
            if (!outDir.exists()) outDir.mkdirs();

            File outputFile = new File(outDir, "ocr_" + System.currentTimeMillis() + ".txt");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(extractedText.getBytes(StandardCharsets.UTF_8));
            }

            Log.d(TAG, "OCR successful, text saved to: " + outputFile.getAbsolutePath());
            result.setOutputUri(Uri.fromFile(outputFile));
            result.setStatus(ConversionResult.Status.SUCCESS);

        } catch (Exception e) {
            Log.e(TAG, "OCR failed: " + e.getMessage());
            result.setStatus(ConversionResult.Status.FAILED);
            result.setMessage("OCR Error: " + e.getMessage());
            throw e;
        }

        return result;
    }
}

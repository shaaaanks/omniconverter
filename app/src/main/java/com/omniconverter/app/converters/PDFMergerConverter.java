package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PDFMergerConverter implements Converter {
    private static final String TAG = "PDFMerger";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        // Extract multiple file URIs from params
        List<Uri> pdfUris = new ArrayList<>();
        pdfUris.add(inputUri);

        Object additionalFilesObj = params.get("additional_files");
        if (additionalFilesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> additionalUris = (List<String>) additionalFilesObj;
            for (String uriStr : additionalUris) {
                pdfUris.add(Uri.parse(uriStr));
            }
        }

        Log.d(TAG, "Merging " + pdfUris.size() + " PDF files");

        // Copy all PDFs to cache
        List<File> pdfFiles = new ArrayList<>();
        for (int i = 0; i < pdfUris.size(); i++) {
            File tmpFile = FileUtils.copyUriToFile(context, pdfUris.get(i), "pdf_" + i + ".pdf");
            pdfFiles.add(tmpFile);
        }

        // Create output file
        File outDir = context.getExternalFilesDir("PDFs");
        if (outDir == null) {
            outDir = new File(context.getCacheDir(), "PDFs");
        }
        if (!outDir.exists()) outDir.mkdirs();

        File mergedPdf = new File(outDir, "merged_" + System.currentTimeMillis() + ".pdf");

        // Simple PDF merge by concatenating bytes
        mergeSimplePDF(pdfFiles, mergedPdf);

        result.setOutputUri(Uri.fromFile(mergedPdf));
        result.setStatus(ConversionResult.Status.SUCCESS);
        Log.d(TAG, "Merge successful: " + mergedPdf.getAbsolutePath());
        return result;
    }

    /**
     * Simple PDF merge by concatenating file contents
     */
    private void mergeSimplePDF(List<File> pdfFiles, File outputFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (File pdfFile : pdfFiles) {
                try (FileInputStream fis = new FileInputStream(pdfFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}



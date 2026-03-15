package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PDFMergerConverter implements Converter {
    private static final String TAG = "PDFMerger";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        List<Uri> pdfUris = new ArrayList<>();

        // Prioritize file_uris array if available
        if (params.containsKey("file_uris")) {
            String[] fileUrisArr = (String[]) params.get("file_uris");
            if (fileUrisArr != null) {
                for (String uriStr : fileUrisArr) {
                    pdfUris.add(Uri.parse(uriStr));
                }
            }
        } else {
            // Fallback to old behavior (inputUri + additional_files)
            pdfUris.add(inputUri);
            Object additionalFilesObj = params.get("additional_files");
            if (additionalFilesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> additionalUris = (List<String>) additionalFilesObj;
                for (String uriStr : additionalUris) {
                    pdfUris.add(Uri.parse(uriStr));
                }
            } else if (additionalFilesObj instanceof String[]) {
                String[] additionalUris = (String[]) additionalFilesObj;
                for (String uriStr : additionalUris) {
                    pdfUris.add(Uri.parse(uriStr));
                }
            }
        }

        if (pdfUris.isEmpty()) {
            throw new IllegalArgumentException("No PDF files provided for merging");
        }

        Log.d(TAG, "Merging " + pdfUris.size() + " PDF files");

        // Copy all PDFs to cache
        List<File> tempFiles = new ArrayList<>();
        PDFMergerUtility merger = new PDFMergerUtility();

        try {
            for (int i = 0; i < pdfUris.size(); i++) {
                File tmpFile = FileUtils.copyUriToFile(context, pdfUris.get(i), "merge_input_" + i + "_" + System.currentTimeMillis() + ".pdf");
                tempFiles.add(tmpFile);
                merger.addSource(tmpFile);
            }

            // Create output file
            File outDir = context.getExternalFilesDir("PDFs");
            if (outDir == null) {
                outDir = new File(context.getCacheDir(), "PDFs");
            }
            if (!outDir.exists()) outDir.mkdirs();

            File mergedPdf = new File(outDir, "merged_" + System.currentTimeMillis() + ".pdf");
            merger.setDestinationFileName(mergedPdf.getAbsolutePath());

            // Perform merge
            merger.mergeDocuments(null);

            result.setOutputUri(Uri.fromFile(mergedPdf));
            result.setStatus(ConversionResult.Status.SUCCESS);
            Log.d(TAG, "Merge successful: " + mergedPdf.getAbsolutePath());
            return result;

        } catch (Exception e) {
            Log.e(TAG, "Merge failed", e);
            throw e;
        } finally {
            // Clean up temporary input files
            for (File f : tempFiles) {
                if (f.exists()) f.delete();
            }
        }
    }
}



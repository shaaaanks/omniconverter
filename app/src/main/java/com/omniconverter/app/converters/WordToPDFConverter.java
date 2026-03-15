package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.core.FileUtils;
import com.omniconverter.app.network.ConvertApiClient;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordToPDFConverter implements Converter {
    private static final String TAG = "WordToPDF";

    @Override
    public ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception {
        ConversionResult result = new ConversionResult(inputUri);

        Log.d(TAG, "Converting Word document to PDF");

        // Get file name
        String fileName = FileUtils.getFileDisplayName(context, inputUri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "document.docx";
        }

        // Try local conversion first for DOCX files
        if (fileName.toLowerCase().endsWith(".docx")) {
            try {
                Log.d(TAG, "Attempting local conversion using Apache POI");
                return convertLocally(context, inputUri, fileName);
            } catch (Exception e) {
                Log.e(TAG, "Local conversion failed: " + e.getMessage());
                e.printStackTrace();
                // Continue to fallback
            }
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

    private ConversionResult convertLocally(Context context, Uri inputUri, String originalFileName) throws Exception {
        // Initialize PDFBox resource loader
        try {
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to initialize PDFBoxResourceLoader: " + t.getMessage());
        }

        File inputFile = FileUtils.copyUriToFile(context, inputUri, "temp_" + originalFileName);

        File outDir = context.getExternalFilesDir("Documents");
        if (outDir == null) outDir = context.getCacheDir();
        if (!outDir.exists()) outDir.mkdirs();

        File outputFile = new File(outDir, "converted_" + System.currentTimeMillis() + ".pdf");

        // Use try-with-resources for auto-closing
        FileInputStream fis = new FileInputStream(inputFile);
        XWPFDocument document = new XWPFDocument(fis);
        PDDocument pdfDoc = new PDDocument();
        PDPageContentStream contentStream = null;

        try {
            PDPage page = new PDPage();
            pdfDoc.addPage(page);

            PDFont font = PDType1Font.HELVETICA;
            float fontSize = 11;
            float leading = 1.5f * fontSize;
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth() - 2 * margin;

            contentStream = new PDPageContentStream(pdfDoc, page);
            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();

                if (text == null || text.trim().isEmpty()) {
                    yPosition -= leading;
                    contentStream.endText(); // End current text block to move
                    if (yPosition < margin) {
                        contentStream.close();
                        page = new PDPage();
                        pdfDoc.addPage(page);
                        contentStream = new PDPageContentStream(pdfDoc, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        contentStream.setFont(font, fontSize);
                    }
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    continue;
                }

                String[] words = text.split(" ");
                float currentLineW = 0;

                for (String word : words) {
                    if (word.isEmpty()) continue;
                    String wordWithSpace = word + " ";
                    float wordW = (font.getStringWidth(wordWithSpace) / 1000.0f) * fontSize;

                    if (currentLineW + wordW > width) {
                        // New line
                        contentStream.endText();
                        yPosition -= leading;

                        if (yPosition < margin) {
                            contentStream.close();
                            page = new PDPage();
                            pdfDoc.addPage(page);
                            contentStream = new PDPageContentStream(pdfDoc, page);
                            yPosition = page.getMediaBox().getHeight() - margin;
                            contentStream.setFont(font, fontSize);
                        }

                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        currentLineW = 0;
                    }

                    contentStream.showText(wordWithSpace);
                    currentLineW += wordW;
                }

                // End paragraph
                contentStream.endText();
                yPosition -= leading * 1.5; // Paragraph spacing

                if (yPosition < margin) {
                    contentStream.close();
                    page = new PDPage();
                    pdfDoc.addPage(page);
                    contentStream = new PDPageContentStream(pdfDoc, page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                    contentStream.setFont(font, fontSize);
                }
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
            }

            contentStream.endText();
            contentStream.close();
            pdfDoc.save(outputFile);

            ConversionResult result = new ConversionResult(inputUri);
            result.setOutputUri(Uri.fromFile(outputFile));
            result.setStatus(ConversionResult.Status.SUCCESS);
            return result;
        } finally {
            if (contentStream != null) contentStream.close();
            pdfDoc.close();
            document.close();
            fis.close();
            if (inputFile.exists()) inputFile.delete();
        }
    }
}

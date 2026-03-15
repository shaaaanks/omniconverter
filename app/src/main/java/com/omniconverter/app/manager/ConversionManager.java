package com.omniconverter.app.manager;

import android.content.Context;
import android.net.Uri;

import com.omniconverter.app.core.ConversionResult;
import com.omniconverter.app.converters.Converter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversionManager {
    private static ConversionManager instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private ConversionManager() {}

    public static synchronized ConversionManager getInstance() {
        if (instance == null) instance = new ConversionManager();
        return instance;
    }

    public void submitConversion(Context context, Converter converter, Uri inputUri, Map<String,Object> params, ConversionCallback callback) {
        executor.submit(() -> {
            try {
                ConversionResult res = converter.convert(context, inputUri, params);
                callback.onComplete(res);
            } catch (Exception e) {
                ConversionResult r = new ConversionResult(inputUri);
                r.setStatus(com.omniconverter.app.core.ConversionResult.Status.FAILED);
                r.setMessage(e.getMessage());
                callback.onComplete(r);
            }
        });
    }

    public interface ConversionCallback {
        void onComplete(ConversionResult result);
    }
}

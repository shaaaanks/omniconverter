package com.omniconverter.app.converters;

import android.content.Context;
import android.net.Uri;

import com.omniconverter.app.core.ConversionResult;

import java.util.Map;

public interface Converter {
    ConversionResult convert(Context context, Uri inputUri, Map<String, Object> params) throws Exception;
}

package com.omniconverter.app.ui.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class FilePickerHelper {
    private static final int PICK_FILE_REQUEST = 1001;

    public static void openFilePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    public static int getPickFileRequestCode() {
        return PICK_FILE_REQUEST;
    }

    public static boolean isFilePicked(int requestCode) {
        return requestCode == PICK_FILE_REQUEST;
    }

    public static Uri getPickedFileUri(Intent data) {
        if (data != null && data.getData() != null) {
            return data.getData();
        }
        return null;
    }
}

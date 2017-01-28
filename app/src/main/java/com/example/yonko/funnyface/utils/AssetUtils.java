package com.example.yonko.funnyface.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AssetUtils {
    private static final String TAG = AssetUtils.class.getSimpleName();

    public static final String FACE_PARTS_DIR = "faceparts";
    public static final String BUBBLES_DIR = "bubbles";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static ArrayList<String> getAssetsFiles(Context context, String path) {
        ArrayList<String> list = null;
        try {
            list = new ArrayList<>(Arrays.asList(context.getAssets().list(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getLastSegmentInURI(final String uri) {
        return uri.replaceFirst(".*/([^/?]+).*", "$1");
    }

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }
}

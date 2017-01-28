package com.example.yonko.funnyface.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.activities.PreferenceActivity;
import com.example.yonko.funnyface.activities.StickerChooserActivity;

public class IntentUtils {
    private static final String TAG = IntentUtils.class.getSimpleName();

    public static final int LOAD_IMAGE_REQUEST = 1;
    public static final int CAMERA_REQUEST = 2;
    public static final int STICKER_CHOOSER_REQUEST = 3;
    public static final int SETTINGS_REQUEST = 4;

    public static void sendStickerChooserRequest(Activity activity, int pagePosition) {
        Intent intent = new Intent(activity, StickerChooserActivity.class);
        if(pagePosition > -1) {
            Bundle bundle = new Bundle();
            bundle.putInt(StickerChooserActivity.POSITION, pagePosition);
            intent.putExtras(bundle);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
            activity.startActivityForResult(intent, IntentUtils.STICKER_CHOOSER_REQUEST, options.toBundle());
        } else {
            activity.startActivityForResult(intent, STICKER_CHOOSER_REQUEST);
        }
    }

    public static Uri sendCameraRequest(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "FunnyFace_Cam_Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "FunnyFace_Captured_from_Camera");
        Uri imageUri = activity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, CAMERA_REQUEST);

        return imageUri;
    }

    public static void sendLoadImageRequest(Activity activity) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, LOAD_IMAGE_REQUEST);
    }

    public static void sendSettingsRequest(Activity activity) {
        Intent intent = new Intent(activity, PreferenceActivity.class);
        activity.startActivityForResult(intent, SETTINGS_REQUEST);
    }
}

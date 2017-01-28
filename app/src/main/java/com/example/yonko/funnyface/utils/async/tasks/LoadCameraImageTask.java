package com.example.yonko.funnyface.utils.async.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.example.yonko.funnyface.utils.BitmapUtils;
import com.example.yonko.funnyface.utils.FileUtils;

import java.io.IOException;

public class LoadCameraImageTask extends AsyncTask<Uri, Void, Bitmap> {
    private static final String TAG = LoadCameraImageTask.class.getSimpleName();

    private Context mContext;
    private LoadCameraImageCallback mLoadCameraImageCallback = null;

    public LoadCameraImageTask(Context mContext, LoadCameraImageCallback mLoadCameraImageCallback) {
        super();
        this.mContext = mContext;
        this.mLoadCameraImageCallback = mLoadCameraImageCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mLoadCameraImageCallback != null) {
            mLoadCameraImageCallback.onPreExecute();
        }
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                mContext.getContentResolver(), uris[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bitmap != null) {
            return BitmapUtils.rotateCameraImage(bitmap, FileUtils.getRealPathFromURI(mContext, uris[0]));
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(mLoadCameraImageCallback != null) {
            mLoadCameraImageCallback.onPostExecute(bitmap);
        }
    }

    public interface LoadCameraImageCallback {
        void onPreExecute();
        void onPostExecute(Bitmap bitmap);
    }
}

package com.example.yonko.funnyface.utils.async.loaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.yonko.funnyface.utils.BitmapUtils;
import com.example.yonko.funnyface.utils.MemoryCache;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class AssetAsyncBitmapLoader {
    private static final String TAG = AssetAsyncBitmapLoader.class.getSimpleName();

    public static void loadBitmap(Resources resources, Bitmap mPlaceHolderBitmap, String resPath, ImageView imageView) {
        if (cancelPotentialWork(resPath, imageView)) {
            final Bitmap bitmap = MemoryCache.getInstance().getBitmapFromMemCache(resPath);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                final BitmapWorkerTask task = new BitmapWorkerTask(resources, imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(resources, mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(resPath);
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if (!equals(bitmapData, data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    public static boolean equals(String str1, String str2) {
        if(str1 != null) {
            return str1.equals(str2);
        } else if(str2 != null) {
            return str2.equals(str1);
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    protected static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = null;
        Resources resources;

        public BitmapWorkerTask(Resources resources, ImageView imageView) {
            this.resources = resources;
            // WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            InputStream inputstream = null;
            Bitmap bitmap = null;
            try {
                inputstream= resources.getAssets().open(data);
                bitmap =  BitmapUtils.decodeSampledBitmapFromResource(inputstream, 100, 100);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            MemoryCache.getInstance().addBitmapToMemoryCache(data, bitmap);
            return bitmap;
        }

        /*
        * Once complete, check if ImageView still exist and set bitmap.
        * */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}

package com.example.yonko.funnyface.utils.async.loaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.yonko.funnyface.utils.BitmapUtils;
import com.example.yonko.funnyface.utils.MemoryCache;

import java.lang.ref.WeakReference;


public class ResourceAsyncBitmapLoader {
    private static final String TAG = ResourceAsyncBitmapLoader.class.getSimpleName();

    public static void loadBitmap(Resources resources, Bitmap mPlaceHolderBitmap, int resId, ImageView imageView) {
        if (cancelPotentialWork(resId, imageView)) {

            final Bitmap bitmap = MemoryCache.getInstance().getBitmapFromMemCache(resId);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                final BitmapWorkerTask task = new BitmapWorkerTask(resources, imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(resources, mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(resId);
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

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            if (bitmapData != data) {
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

    protected static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        Resources resources;

        public BitmapWorkerTask(Resources resources, ImageView imageView) {
            this.resources = resources;
            // WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            final Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromResource(resources, data, 100, 100);
            MemoryCache.getInstance().addBitmapToMemoryCache(params[0], bitmap);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
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

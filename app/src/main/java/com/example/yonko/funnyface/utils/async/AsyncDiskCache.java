package com.example.yonko.funnyface.utils.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.yonko.funnyface.BuildConfig;
import com.example.yonko.funnyface.utils.FileUtils;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Only getBitmap is not on background thread!
public class AsyncDiskCache {
    private static final String TAG = AsyncDiskCache.class.getSimpleName();
    private static int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static AsyncDiskCache INSTANCE = null;

    private DiskLruCache mDiskCache;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private AsyncDiskCache(Context context, String uniqueName, int diskCacheSize,
                           Bitmap.CompressFormat compressFormat, int quality) {
        final File diskCacheDir = getDiskCacheDir(context, uniqueName);
        new InitDiskCacheTask().execute(diskCacheDir);
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }

    private synchronized static void createInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AsyncDiskCache(context, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE, Bitmap.CompressFormat.JPEG, DEFAULT_COMPRESS_QUALITY);
        }
    }

    public static AsyncDiskCache getInstance(Context context) {
        if (INSTANCE == null) createInstance(context);
        return INSTANCE;
    }

    public void put(String key, Bitmap data, boolean forRecycle) {
        if (mDiskCache != null) {
            new SaveToDiskCacheTask(key, forRecycle).execute(data);
        }
    }

    public Bitmap getBitmap(String key) {
        Bitmap bitmap = null;
//        if((bitmap = MemoryCache.getInstance().getBitmapFromMemCache(key))!= null) {
//            return bitmap;
//        }
        Log.i(TAG, "Not in");
        DiskLruCache.Snapshot snapshot = null;

        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskCache != null) {
                try {
                    snapshot = mDiskCache.get(key);
                    if (snapshot == null) {
                        return null;
                    }
                    final InputStream in = snapshot.getInputStream(0);
                    if (in != null) {
                        final BufferedInputStream buffIn =
                                new BufferedInputStream(in, IO_BUFFER_SIZE);
                        bitmap = BitmapFactory.decodeStream(buffIn);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (snapshot != null) {
                        snapshot.close();
                    }
                }
                if (BuildConfig.DEBUG) {
                    Log.d("cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
                }
            }
        }

        return bitmap;
    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if (BuildConfig.DEBUG) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                try {
                    mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, DISK_CACHE_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    class SaveToDiskCacheTask extends AsyncTask<Bitmap, Void, Void> {
        private String mKey;
        private boolean mForRecycle;

        public SaveToDiskCacheTask(String key, boolean forRecycle) {
            mKey = key;
            mForRecycle = forRecycle;
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            synchronized (mDiskCacheLock) {
                Bitmap data = params[0];
                //MemoryCache.getInstance().replaceBitmapToMemoryCache(mKey, data);
                DiskLruCache.Editor editor = null;
                try {
                    editor = mDiskCache.edit(mKey);
                    if (editor != null) {
                        if (writeBitmapToFile(data, editor)) {
                            mDiskCache.flush();
                            editor.commit();
                            if (BuildConfig.DEBUG) {
                                Log.d("cache_test_DISK_", "image put on disk cache " + mKey);
                            }
                        } else {
                            editor.abort();
                            if (BuildConfig.DEBUG) {
                                Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + mKey);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + mKey);
                    }
                    try {
                        if (editor != null) {
                            editor.abort();
                        }
                    } catch (IOException ignored) {
                    }
                }
                if(mForRecycle && data != null) {
                    data.recycle();
                }
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !FileUtils.isExternalStorageRemovable() ?
                        FileUtils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
}
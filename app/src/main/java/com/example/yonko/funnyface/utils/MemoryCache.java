package com.example.yonko.funnyface.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class MemoryCache {

    private static MemoryCache INSTANCE = null;
    private LruCache<String, Bitmap> mMemoryCache;

    private MemoryCache() {
        initLRUCache();
    }

    private synchronized static void createInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MemoryCache();
        }
    }

    public static MemoryCache getInstance() {
        if(INSTANCE == null) createInstance();
        return INSTANCE;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void replaceBitmapToMemoryCache(String key, Bitmap bitmap) {
        mMemoryCache.put(key, bitmap);
    }

    public void addBitmapToMemoryCache(int resKey, Bitmap bitmap) {
        addBitmapToMemoryCache(String.valueOf(resKey), bitmap);
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap getBitmapFromMemCache(int resKey) {
        return getBitmapFromMemCache(String.valueOf(resKey));
    }

    private void initLRUCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 12;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }
}

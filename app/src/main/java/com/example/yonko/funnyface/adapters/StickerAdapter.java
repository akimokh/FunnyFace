package com.example.yonko.funnyface.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.utils.async.loaders.AssetAsyncBitmapLoader;

import java.util.ArrayList;

public class StickerAdapter extends BaseAdapter {
    private static final String TAG = StickerAdapter.class.getSimpleName();

    private final Context mContext;
    private String mDirPath;
    private ArrayList<String> mImages;
    private Bitmap mDefaultImage;

    public StickerAdapter(Context context) {
        super();
        this.mContext = context;
    }

    public StickerAdapter(Context context, String dirPath, ArrayList<String> images, Bitmap defaultImage) {
        this(context);
        mDirPath = dirPath;
        mImages = images;
        mDefaultImage = defaultImage;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_sticker_holder, container, false);
        }

        AssetAsyncBitmapLoader.loadBitmap(mContext.getResources(), mDefaultImage, mDirPath + mImages.get(position), (ImageView) convertView);
        return convertView;
    }
}

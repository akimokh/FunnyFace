package com.example.yonko.funnyface.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.yonko.funnyface.fragments.StickerGridFragment;
import com.example.yonko.funnyface.utils.AssetUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class GridFragmentPagerAdapter extends FragmentPagerAdapter implements StickerGridFragment.ItemClickCallback{
    private static final String TAG = GridFragmentPagerAdapter.class.getSimpleName();

    private ArrayList<String> mCategories = null;
    private TreeMap<String, ArrayList<String>> mFaceParts;
    private ItemClickCallback mItemClickCallback;

    public GridFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mCategories = AssetUtils.getAssetsFiles(context, AssetUtils.FACE_PARTS_DIR);
        mFaceParts = new TreeMap<>();
        for(String dir : mCategories) {
            ArrayList<String> images = AssetUtils.getAssetsFiles(context, AssetUtils.FACE_PARTS_DIR + File.separator + dir);
            mFaceParts.put(dir, images);
        }
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Fragment getItem(int position) {
        StickerGridFragment stickerGridFragment = StickerGridFragment.newInstance(
                new StickerGridFragment.DataBundle(AssetUtils.FACE_PARTS_DIR + File.separator + mCategories.get(position) + File.separator,
                        mFaceParts.get(mCategories.get(position))));
        stickerGridFragment.setOnItemClickCallback(this);
        return stickerGridFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategories.get(position);
    }

    @Override
    public void onItemClick(Object item) {
        if(mItemClickCallback != null) {
            mItemClickCallback.onItemClick(item);
        }
    }

    public void setOnItemClickCallback(ItemClickCallback mItemClickCallback) {
        this.mItemClickCallback = mItemClickCallback;
    }

    public interface ItemClickCallback {
        void onItemClick(Object item);
    }
}

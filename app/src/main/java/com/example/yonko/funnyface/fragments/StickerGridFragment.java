package com.example.yonko.funnyface.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.adapters.StickerAdapter;

import java.io.Serializable;
import java.util.ArrayList;

public class StickerGridFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = StickerGridFragment.class.getSimpleName();

    public static final String ARG_BUNDLE = "ARG_BUNDLE";
    
    private DataBundle mFacePartsBundle;
    private ItemClickCallback mItemClickCallback;
    private DialogCallback mDialogCallback;

    private StickerAdapter mAdapter;
    private GridView mGridView;
    private Bitmap mDefaultImage;
    private String mStickersDir;

    public static StickerGridFragment newInstance(DataBundle dataBundle) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BUNDLE, dataBundle);
        StickerGridFragment fragment = new StickerGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StickerGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFacePartsBundle = (DataBundle) getArguments().getSerializable(ARG_BUNDLE);
        mStickersDir = (mFacePartsBundle != null)? mFacePartsBundle.getDirectory() : "";
        mDefaultImage = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if(dialog != null) {
            dialog.getWindow()
                    .getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        mAdapter = new StickerAdapter(getActivity(), mStickersDir, mFacePartsBundle.getStickers(), mDefaultImage);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_grid_page, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(mItemClickCallback != null) {
            mItemClickCallback.onItemClick(mStickersDir + mAdapter.getItem(i));
        }
        if(mDialogCallback != null) {
            mDialogCallback.onFinnish((BitmapDrawable)((ImageView) view).getDrawable());
            dismiss();
        }
    }

    public void setOnItemClickCallback(ItemClickCallback itemClickCallback) {
        mItemClickCallback = itemClickCallback;
    }

    public void setDialogCallback(DialogCallback dialogCallback) {
        mDialogCallback = dialogCallback;
    }

    public interface ItemClickCallback {
        void onItemClick(Object item);
    }

    public interface DialogCallback {
        void onFinnish(BitmapDrawable bitmapDrawable);
    }

    public static class DataBundle implements Serializable {
        private ArrayList<String> mStickers;
        private String mDirectory;

        public DataBundle(String directory, ArrayList<String> stickers) {
            mDirectory = directory;
            mStickers = stickers;
        }

        public ArrayList<String> getStickers() {
            return mStickers;
        }
        public String getDirectory() {
            return mDirectory;
        }
    }
}
package com.example.yonko.funnyface.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.example.yonko.funnyface.models.EditorFab;
import com.example.yonko.funnyface.dialogs.BubbleInputDialog;
import com.example.yonko.funnyface.fragments.ColorPickerDialogFragment;
import com.example.yonko.funnyface.models.Fab;
import com.example.yonko.funnyface.widgets.BubbleTextView;
import com.example.yonko.funnyface.widgets.CanvasView;
import com.example.yonko.funnyface.widgets.StickerView;
import com.example.yonko.funnyface.fragments.StickerGridFragment;
import com.example.yonko.funnyface.utils.AssetUtils;

import java.io.File;
import java.util.ArrayList;

public class EditorManager implements StickerGridFragment.DialogCallback, ColorPickerDialogFragment.ColorPickerDialogListener {
    private static final String TAG = EditorManager.class.getSimpleName();

    // =========== COMMON VARIABLES ==============
    private Context mContext;
    private CanvasView mContentView;
    private BubbleInputDialog mBubbleInputDialog;
    private FragmentManager mFragmentManager;
    private Fab mSaveCallbackFab = null;

    // =========== STICKER VARIABLES ==============
    private StickerView mCurrentView;
    private BubbleTextView mCurrentEditTextView;

    private ArrayList<StickerView> mStickers = new ArrayList<>();
    private ArrayList<BubbleTextView> mTextBubbles = new ArrayList<>();

    // =========== TOUCH EFFECT VARIABLES ==============
    private ImageMorpher mImageMorpher;
    private ImageDrawer mImageDrawer;

    public EditorManager(Context context, CanvasView contentView, FragmentManager fragmentManager, Bitmap image) {
        mContext = context;
        mContentView = contentView;
        mFragmentManager = fragmentManager;
        init(mContentView, image);
    }

    void init(final CanvasView contentView, final Bitmap image) {
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        contentView.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                        contentView.scaleToImage(image);

                        mBubbleInputDialog = new BubbleInputDialog(mContext);
                        mBubbleInputDialog.setCompleteCallBack(new BubbleInputDialog.CompleteCallBack() {
                            @Override
                            public void onComplete(View bubbleTextView, String str) {
                                ((BubbleTextView) bubbleTextView).setText(str);
                            }
                        });
                    }
                });
    }

    public void setSaveCallbackFab(Fab fab) {
        mSaveCallbackFab = fab;
    }

    //
    public void startMorphing() {
        if (mContentView != null) {
            if (mImageMorpher == null) {
                mImageMorpher = new ImageMorpher();
            }

            if (mImageDrawer != null && mImageDrawer.isAttached()) {
                mImageDrawer.setDrawingView(null);
            }

            mImageMorpher.setDrawingView(mContentView);
            mImageMorpher.initMorpher();
            mContentView.isBaseDrawingEnabled(false);
        }
    }

    public void stopMorphing() {
        if (mContentView != null && mImageMorpher != null) {
            mImageMorpher.setDrawingView(null);
        }
    }

    public boolean undoMorph() {
        if (mContentView != null && mImageMorpher != null && mImageMorpher.isAttached()) {
            return mImageMorpher.onClickUndo();
        }
        return false;
    }

    public boolean redoMorph() {
        if (mContentView != null && mImageMorpher != null && mImageMorpher.isAttached()) {
            return mImageMorpher.onClickRedo();
        }
        return false;
    }

    public boolean isMorpherUndoActive() {
        if(mImageMorpher != null) {
            return mImageMorpher.isUndoActive();
        }
        return false;
    }

    public boolean isMorpherRedoActive() {
        if(mImageMorpher != null) {
            return mImageMorpher.isRedoActive();
        }
        return false;
    }

    public void startDrawing() {
        if (mContentView != null) {
            if (mImageDrawer == null) {
                mImageDrawer = new ImageDrawer();
            }

            if (mImageMorpher != null && mImageMorpher.isAttached()) {
                mImageMorpher.setDrawingView(null);
            }

            mImageDrawer.setDrawingView(mContentView);
            mImageDrawer.initPaint();
            mContentView.isBaseDrawingEnabled(true);
        }
    }

    public void stopDrawing() {
        if (mContentView != null && mImageDrawer != null) {
            mImageDrawer.setDrawingView(null);
        }
    }

    public boolean undoPaint() {
        if (mContentView != null && mImageDrawer != null && mImageDrawer.isAttached()) {
            return mImageDrawer.onClickUndo();
        }
        return false;
    }


    public boolean redoPaint() {
        if (mContentView != null && mImageDrawer != null && mImageDrawer.isAttached()) {
            return mImageDrawer.onClickRedo();
        }
        return false;
    }

    public boolean isDrawerUndoActive() {
        if(mImageDrawer != null) {
            return mImageDrawer.isUndoActive();
        }
        return false;
    }

    public boolean isDrawerRedoActive() {
        if(mImageDrawer != null) {
            return mImageDrawer.isRedoActive();
        }
        return false;
    }

    public void setDrawingBrush() {
        if (mFragmentManager != null) {
            ColorPickerDialogFragment brushPicker = ColorPickerDialogFragment.newInstance(getDrawerBrushColor(), getDrawerBrushSize());
            brushPicker.setColoPickerDialogListener(this);
            brushPicker.show(mFragmentManager, "brush_chooser");
        }
    }

    public void clearUndo() {
        clearMorpherUndo();
        clearDrawerUndo();
    }

    public void setVisibility(boolean visibility) {
        setMorpherVisibility(visibility);
        setDrawerVisibility(visibility);
        setStickersVisibility(visibility);
        setTextBubblesVisibility(visibility);
    }

    public void setDrawerVisibility(boolean visible) {
        if(mImageDrawer != null) {
            mImageDrawer.setVisibility(visible);
        }
    }

    public void setMorpherVisibility(boolean visible) {
        if(mImageMorpher != null) {
            mImageMorpher.setVisibility(visible);
        }
    }

    // =========== STICKER FUNCTIONS ==============
    public void addStickerView(Bitmap bitmap) {
        if (mContentView != null) {
            final StickerView stickerView = new StickerView(mContext);
            stickerView.setBitmap(bitmap);
            stickerView.setOperationListener(new StickerView.OperationListener() {
                @Override
                public void onDeleteClick() {
                    mContentView.removeView(stickerView);
                    mStickers.remove(stickerView);
                    if(mStickers.size() == 0) {
                        hideSaveCallbackFab();
                    }
                }

                @Override
                public void onEdit(StickerView stickerView) {
                    if (mCurrentEditTextView != null) {
                        mCurrentEditTextView.setInEdit(false);
                    }
                    mCurrentView.setInEdit(false);
                    mCurrentView = stickerView;
                    mCurrentView.setInEdit(true);
                }

                @Override
                public void onTop(StickerView stickerView) {
                    // may need later
                }
            });
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mContentView.addView(stickerView, lp);
            setCurrentEdit(stickerView);
            mStickers.add(stickerView);
            showSaveCallbackFab();
        }
    }

    private void addBubbleText(Bitmap bitmap) {
        if (mContentView != null) {
            final BubbleTextView bubbleTextView = new BubbleTextView(mContext,
                    Color.BLACK);
            bubbleTextView.setBitmap(bitmap);
            bubbleTextView.setOperationListener(new BubbleTextView.OperationListener() {
                @Override
                public void onDeleteClick() {
                    mTextBubbles.remove(bubbleTextView);
                    mContentView.removeView(bubbleTextView);
                    if(mTextBubbles.size() == 0) {
                        hideSaveCallbackFab();
                    }
                }

                @Override
                public void onEdit(BubbleTextView bubbleTextView) {
                    if (mCurrentView != null) {
                        mCurrentView.setInEdit(false);
                    }
                    mCurrentEditTextView.setInEdit(false);
                    mCurrentEditTextView = bubbleTextView;
                    mCurrentEditTextView.setInEdit(true);
                }

                @Override
                public void onClick(BubbleTextView bubbleTextView) {
                    mBubbleInputDialog.setBubbleTextView(bubbleTextView);
                    mBubbleInputDialog.show();
                }

                @Override
                public void onTop(BubbleTextView bubbleTextView) {
                    // may need later
                }
            });
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mContentView.addView(bubbleTextView, lp);
            setCurrentEdit(bubbleTextView);
            mTextBubbles.add(bubbleTextView);
            Log.i(TAG, "addBubbleText");
            showSaveCallbackFab();
        }
    }

    public void addBubbleTextView() {
        if (mFragmentManager != null) {
            ArrayList<String> images = AssetUtils.getAssetsFiles(mContext, AssetUtils.BUBBLES_DIR);
            StickerGridFragment bubbleChooser = StickerGridFragment.newInstance(new StickerGridFragment.DataBundle(AssetUtils.BUBBLES_DIR + File.separator, images));
            bubbleChooser.setDialogCallback(this);
            bubbleChooser.show(mFragmentManager, "bubble_chooser");
        }
    }

    public void setStickerInEdit(boolean inEdit) {
        if (mCurrentView != null) {
            mCurrentView.setInEdit(inEdit);
        }
    }

    public void setBubbleTextInEdit(boolean inEdit) {
        if (mCurrentEditTextView != null) {
            mCurrentEditTextView.setInEdit(inEdit);
        }
    }

    public void setStickersVisibility(boolean visible) {
        if (mContentView != null) {
            for (View stickerView : mTextBubbles) {
                if (visible) {
                    stickerView.setVisibility(View.VISIBLE);
                } else {
                    stickerView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setTextBubblesVisibility(boolean visible) {
        if (mContentView != null) {
            for (StickerView stickerView : mStickers) {
                if (visible) {
                    stickerView.setVisibility(View.VISIBLE);
                } else {
                    stickerView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

/*    public void removeCurrentSticker() {
        if(mContentView != null && mCurrentView != null) {
            mContentView.removeView(mCurrentView);
            mStickers.remove(mCurrentView);
        }
    }

    public void removeCurrentBubbleText() {
        if(mContentView != null && mCurrentEditTextView != null) {
            mContentView.removeView(mCurrentEditTextView);
            mTextBubbles.remove(mCurrentEditTextView);
        }
    }*/

    public void removeAllStickers() {
        if (mContentView != null) {
            for (StickerView stickerView : mStickers) {
                mContentView.removeView(stickerView);
            }
            mStickers.clear();
            hideSaveCallbackFab();
        }
    }

    public void removeAllTextBubbles() {
        if (mContentView != null) {
            for (BubbleTextView bubbleTextView : mTextBubbles) {
                mContentView.removeView(bubbleTextView);
            }
            mTextBubbles.clear();
            hideSaveCallbackFab();
        }
    }

    public void reset() {
        removeAllStickers();
        removeAllTextBubbles();
        stopDrawing();
        stopMorphing();
    }

    public Bitmap generateBitmap() {
        Bitmap bitmap = null;
        setStickerInEdit(false);
        setBubbleTextInEdit(false);
        if (mContentView != null) {
            bitmap = Bitmap.createBitmap(mContentView.getWidth(),
                    mContentView.getHeight()
                    , Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mContentView.draw(canvas);
        }
        return bitmap;
    }

    private void setCurrentEdit(StickerView stickerView) {
        setStickerInEdit(false);
        setBubbleTextInEdit(false);
        mCurrentView = stickerView;
        stickerView.setInEdit(true);
    }

    private void setCurrentEdit(BubbleTextView bubbleTextView) {
        setStickerInEdit(false);
        setBubbleTextInEdit(false);
        mCurrentEditTextView = bubbleTextView;
        mCurrentEditTextView.setInEdit(true);
    }

    private void clearDrawerUndo() {
        if(mImageDrawer != null) {
            mImageDrawer.clearUndo();
        }
    }

    private void clearMorpherUndo() {
        if (mImageMorpher != null) {
            mImageMorpher.clearUndo();
        }
    }

    private void setDrawerBrushColor(int color) {
        if(mImageDrawer != null) {
            mImageDrawer.setBrushColor(color);
        }
    }
    private int getDrawerBrushColor() {
        if(mImageDrawer != null) {
            return mImageDrawer.getBrushColor();
        }
        return ImageDrawer.DEFAULT_BRUSH_COLOR;
    }

    private void setDrawerBrushSize(float size) {
        if(mImageDrawer != null) {
            mImageDrawer.setBrushSize(size);
        }
    }
    private float getDrawerBrushSize() {
        if (mImageDrawer != null) {
            return mImageDrawer.getCurrentBrushSize();
        }
        return ImageDrawer.DEFAULT_BRUSH_SIZE;
    }

    private void showSaveCallbackFab() {
        if (mSaveCallbackFab != null) {
            mSaveCallbackFab.show();
        }
    }

    private void hideSaveCallbackFab() {
        if (mSaveCallbackFab != null) {
            mSaveCallbackFab.hide();
        }
    }

    @Override
    public void onFinnish(BitmapDrawable bitmapDrawable) {
        addBubbleText(bitmapDrawable.getBitmap());
        showSaveCallbackFab();
    }

    @Override
    public void onFinishColorPickerDialog(int color, float brushSize) {
        setDrawerBrushColor(color);
        setDrawerBrushSize(brushSize);
    }
}

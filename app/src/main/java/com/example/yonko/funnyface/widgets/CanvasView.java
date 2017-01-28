package com.example.yonko.funnyface.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.NoSuchElementException;

public class CanvasView extends FrameLayout {
    private static final String TAG = CanvasView.class.getSimpleName();

    MyCanvasCallback drawingCallback;
    boolean baseDrawingEnabled = true;

    public CanvasView(Context context) {
        super(context);
        setFocusable(true);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    }

    public void setDrawingCallback(MyCanvasCallback myCanvasCallback) {
        this.drawingCallback = myCanvasCallback;
    }

    public void isBaseDrawingEnabled(boolean baseDrawingEnabled) {
        this.baseDrawingEnabled = baseDrawingEnabled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (baseDrawingEnabled) {
            super.onDraw(canvas);
        }
        if (drawingCallback != null) {
            drawingCallback.onDraw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean returnValue = super.onTouchEvent(event);
        if (drawingCallback != null) {
            returnValue = drawingCallback.onTouchEvent(event);
        }

        return returnValue;
    }

    public interface MyCanvasCallback {
        void onDraw(Canvas canvas);

        boolean onTouchEvent(MotionEvent event);
    }

    // If not drawable is provided, it`s scale to his current background
    public Bitmap scaleToImage(Bitmap bitmap) {
        boolean forRecycle = false;
        if (bitmap == null) {
            forRecycle = true;
            bitmap = ((BitmapDrawable) this.getBackground()).getBitmap();
        }

        // Get current dimensions AND the desired bounding box
        int boundingX = ((ViewGroup) this.getParent()).getWidth();
        int boundingY = ((ViewGroup) this.getParent()).getHeight();

        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        //int bounding = dpToPx(250);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side.
        float xScale = ((float) boundingX) / width;
        float yScale = ((float) boundingY) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        //get old coordinates
        int lastX = getLeft();
        int lastY = getTop();

        // Apply the scaled bitmap
        this.setBackground(result);

        // Now change ImageView's dimensions to match the scaled image
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        params.width = width;
        params.height = height;
        this.setLayoutParams(params);
        this.setLeft(lastX);
        this.setTop(lastY);
        if(forRecycle) {
            bitmap.recycle();
        }

        return scaledBitmap;
    }

    public Bitmap generateBitmap() {
        Bitmap bitmap = null;
            bitmap = Bitmap.createBitmap(getWidth(), getHeight()
                    , Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            draw(canvas);
        return bitmap;
    }

    public Drawable generateBitmapDrawable() {
        Bitmap bitmap = generateBitmap();
        Drawable drawable = new BitmapDrawable(generateBitmap());
        bitmap.recycle();
        return drawable;
    }

    public BitmapDrawable save() {
        BitmapDrawable drawable = (BitmapDrawable) generateBitmapDrawable();
        setBackground(drawable);
        return drawable;
    }
}

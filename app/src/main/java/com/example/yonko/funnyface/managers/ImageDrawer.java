package com.example.yonko.funnyface.managers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.example.yonko.funnyface.widgets.CanvasView;

import java.util.ArrayList;

public class ImageDrawer implements CanvasView.MyCanvasCallback {
    private static final String TAG = ImageDrawer.class.getSimpleName();

    public static int DEFAULT_BRUSH_COLOR = Color.WHITE;
    public static float DEFAULT_BRUSH_SIZE = 6;

    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<PathHolder> mPaths = new ArrayList<>();
    private ArrayList<PathHolder> mUndonePaths = new ArrayList<>();
    private boolean attached = false;
    private boolean visible = false;
    private int mCurrentBrushColor = DEFAULT_BRUSH_COLOR;
    private float mCurrentBrushSize = DEFAULT_BRUSH_SIZE;
    private CanvasView mDrawingView;

    public boolean isAttached() {
        return this.attached;
    }

    public void setDrawingView(CanvasView myCanvas) {
        if (myCanvas == null) {
            if(this.mDrawingView != null) {
                this.mDrawingView.setDrawingCallback(null);
            }
            attached = false;
            mPaths.clear();
            mUndonePaths.clear();
        } else {
            attached = true;
            myCanvas.setDrawingCallback(this);
        }
        this.mDrawingView = myCanvas;
    }

    void initPaint() {
        if (mDrawingView != null) {
            mDrawingView.setFocusable(true);
            mDrawingView.setFocusableInTouchMode(true);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(DEFAULT_BRUSH_COLOR);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(DEFAULT_BRUSH_SIZE);
            mCanvas = new Canvas();
            mPath = new Path();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(visible) {
            for (PathHolder p : mPaths) {
                mPaint.setColor(p.getBrushColor());
                mPaint.setStrokeWidth(p.getBrushSize());
                canvas.drawPath(p.getPath(), mPaint);
            }
            mPaint.setColor(mCurrentBrushColor);
            mPaint.setStrokeWidth(mCurrentBrushSize);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (mCanvas != null) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    mDrawingView.invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    mDrawingView.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    mDrawingView.invalidate();
                    break;
            }
            handled = true;
        }
        return handled;
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mUndonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        if(!visible) visible = true;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mPaths.add(new PathHolder(mPath, mCurrentBrushColor, mCurrentBrushSize));
        mPath = new Path();
    }

    public void setBrushColor(int color) {
        this.mCurrentBrushColor = color;
    }
    public int getBrushColor() {
        return this.mCurrentBrushColor;
    }

    public void setBrushSize(float size) {
        this.mCurrentBrushSize = size;
    }
    public float getCurrentBrushSize() {
        return this.mCurrentBrushSize;
    }

    public void clearUndo() {
        mPaths.clear();
    }

    /*
    * Change visibility of the painted paths
     * */
    public void setVisibility(boolean visible) {
        this.visible = visible;
        if(mDrawingView != null) {
            mDrawingView.invalidate();
        }
    }

    /*
    *  Handling Undo click*/
    public boolean onClickUndo() {
        if (mDrawingView != null && mPaths.size() > 0) {
            mUndonePaths.add(mPaths.remove(mPaths.size() - 1));
            mDrawingView.invalidate();
            return mPaths.size() > 0;
        }
        return isUndoActive();
    }

    public boolean isUndoActive() {
        return mPaths.size() > 0;
    }

    /*
    * Handling Redo click
    * */
    public boolean onClickRedo() {
        if (mDrawingView != null && mUndonePaths.size() > 0) {
            mPaths.add(mUndonePaths.remove(mUndonePaths.size() - 1));
            mDrawingView.invalidate();
            return isRedoActive();
        }
        return false;
    }

    public boolean isRedoActive() {
        return mUndonePaths.size() > 0;
    }

    private static class PathHolder {
        private Path mPath;
        private int mBrushColor;
        private float mBrushSize;

        public PathHolder(Path path, int brushColor, float brushSize) {
            mPath = path;
            mBrushColor = brushColor;
            mBrushSize = brushSize;
        }

        public void setPath(Path path) {
            mPath = path;
        }
        public Path getPath() {
            return mPath;
        }

        public void setBrushColor(int brushColor) {
            mBrushColor = brushColor;
        }
        public int getBrushColor() {
            return mBrushColor;
        }

        public void setBrushSize(float brushSize) {
            mBrushSize = brushSize;
        }
        public float getBrushSize() {
            return mBrushSize;
        }
    }
}
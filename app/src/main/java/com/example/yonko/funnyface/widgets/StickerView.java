package com.example.yonko.funnyface.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.yonko.funnyface.R;

public class StickerView extends ImageView {
    private static final String TAG = StickerView.class.getSimpleName();

    private Bitmap deleteBitmap;
    private Bitmap flipVBitmap;
    private Bitmap topBitmap;
    private Bitmap resizeBitmap;
    private Bitmap mBitmap;
    private Rect mRectDelete;
    private Rect mRectResize;
    private Rect mRectFlip;
    private Rect mRectTop;
    private int deleteBitmapWidth;
    private int deleteBitmapHeight;
    private int resizeBitmapWidth;
    private int resizeBitmapHeight;
    private int flipVBitmapWidth;
    private int flipVBitmapHeight;
    
    private int topBitmapWidth;
    private int topBitmapHeight;
    private Paint localPaint; // Used for the bounding box
    private int mScreenwidth, mScreenHeight;
    private static final float BITMAP_SCALE = 0.7f; // Scaling factor for the boundaries of the control buttons
    private PointF mid = new PointF();
    private float lastRotateDegree;
    private OperationListener operationListener;

    //Is the second finger down
    private boolean isPointerDown = false;
    //Finger moving distance must exceed this value
    private final float pointerLimitDis = 20f;
    private final float pointerZoomCoeff = 0.09f;
    private float lastDialognalLength;
    private boolean isInResize = false;
    private Matrix mMatrix = new Matrix(); // Matrix used for transformations
    private boolean isInside;
    private float lastX, lastY;
    private boolean isInEdit = true;
    private float MIN_SCALE = 0.8f;
    private float MAX_SCALE = 1.2f;
    private double halfDiagonalLength;

    private float mOriginalWidth = 0; // The initial width of the sticker bitmap
    private float oldDis; // Used in pinch resizing
    private boolean isFlipped = false; // Used for the flip button

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRectDelete = new Rect(); // Rectangle used for delete button
        mRectResize = new Rect(); // Resize button
        mRectFlip = new Rect(); // Flip button
        mRectTop = new Rect(); // Top button
        localPaint = new Paint();
        localPaint.setColor(getResources().getColor(R.color.red_e73a3d)); // bounding box color
        localPaint.setAntiAlias(true);
        localPaint.setDither(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeWidth(2.0f);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenwidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            float[] arrayOfFloat = new float[9];
            mMatrix.getValues(arrayOfFloat); // Getting the values from transformation matrix
            float topLeftX = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2]; // Transition of x -- global x
            float topLeftY = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5]; // Transition of y -- global y
            float topRightX = arrayOfFloat[0] * mBitmap.getWidth() + 0.0F * arrayOfFloat[1] + arrayOfFloat[2]; // Top right  x
            float topRightY = arrayOfFloat[3] * mBitmap.getWidth() + 0.0F * arrayOfFloat[4] + arrayOfFloat[5]; // Top right  y
            float bottomLeftX = 0.0F * arrayOfFloat[0] + arrayOfFloat[1] * mBitmap.getHeight() + arrayOfFloat[2]; // Bottom left x
            float bottomLeftY = 0.0F * arrayOfFloat[3] + arrayOfFloat[4] * mBitmap.getHeight() + arrayOfFloat[5]; // Bottom left y
            float bottomRightX = arrayOfFloat[0] * mBitmap.getWidth() + arrayOfFloat[1] * mBitmap.getHeight() + arrayOfFloat[2]; // Bottom right x
            float bottomRightY = arrayOfFloat[3] * mBitmap.getWidth() + arrayOfFloat[4] * mBitmap.getHeight() + arrayOfFloat[5]; // Bottom right y

            canvas.save();
            canvas.drawBitmap(mBitmap, mMatrix, null);
            // The remove button in the bottom right corner
            mRectDelete.left = (int) (topRightX - deleteBitmapWidth / 2);
            mRectDelete.right = (int) (topRightX + deleteBitmapWidth / 2);
            mRectDelete.top = (int) (topRightY - deleteBitmapHeight / 2);
            mRectDelete.bottom = (int) (topRightY + deleteBitmapHeight / 2);
            // The stretching button in the bottom right corner
            mRectResize.left = (int) (bottomRightX - resizeBitmapWidth / 2);
            mRectResize.right = (int) (bottomRightX + resizeBitmapWidth / 2);
            mRectResize.top = (int) (bottomRightY - resizeBitmapHeight / 2);
            mRectResize.bottom = (int) (bottomRightY + resizeBitmapHeight / 2);
            // The BringToTop button in the top left corner
            mRectTop.left = (int) (topLeftX - flipVBitmapWidth / 2);
            mRectTop.right = (int) (topLeftX + flipVBitmapWidth / 2);
            mRectTop.top = (int) (topLeftY - flipVBitmapHeight / 2);
            mRectTop.bottom = (int) (topLeftY + flipVBitmapHeight / 2);
            // The flip button in the bottom left corner
            mRectFlip.left = (int) (bottomLeftX - topBitmapWidth / 2);
            mRectFlip.right = (int) (bottomLeftX + topBitmapWidth / 2);
            mRectFlip.top = (int) (bottomLeftY - topBitmapHeight / 2);
            mRectFlip.bottom = (int) (bottomLeftY + topBitmapHeight / 2);

            if (isInEdit) {
                // Draw bounding box
                canvas.drawLine(topLeftX, topLeftY, topRightX, topRightY, localPaint);
                canvas.drawLine(topRightX, topRightY, bottomRightX, bottomRightY, localPaint);
                canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY, localPaint);
                canvas.drawLine(bottomLeftX, bottomLeftY, topLeftX, topLeftY, localPaint);

                // Draw buttons
                canvas.drawBitmap(deleteBitmap, null, mRectDelete, null);
                canvas.drawBitmap(resizeBitmap, null, mRectResize, null);
                canvas.drawBitmap(flipVBitmap, null, mRectFlip, null);
                canvas.drawBitmap(topBitmap, null, mRectTop, null);
            }
            canvas.restore();
        }
    }

    @Override
    public void setImageResource(int resId) {
        setBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    public void setBitmap(Bitmap bitmap) {
        mMatrix.reset();
        mBitmap = bitmap;
        setDiagonalLength();
        initBitmaps();
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        mOriginalWidth = w;
        float initScale = (MIN_SCALE + MAX_SCALE) / 2; // Calculate average initial scale
        mMatrix.postScale(initScale, initScale, w / 2, h / 2); // Set scale from the center
        mMatrix.postTranslate(mScreenwidth / 2 - w / 2, (mScreenwidth) / 2 - h / 2); // Center the sticker
        invalidate(); // Call for redraw
    }

    /*
    * Initializing the halfDiagonalLength variable
    * */
    private void setDiagonalLength() {
        halfDiagonalLength = Math.hypot(mBitmap.getWidth(), mBitmap.getHeight()) / 2;
    }

    private void initBitmaps() {
        // When the image is wider than tall, MIN_SCALE is calculated according to be minimum 1/8
        // of the screen and the MAX_SCALE maximum to the screen width
        if (mBitmap.getWidth() >= mBitmap.getHeight()) {
            float minWidth = mScreenwidth / 8;
            if (mBitmap.getWidth() < minWidth) {
                MIN_SCALE = 1f;  // TO-DO maybe better to set it right
            } else {
                MIN_SCALE = 1.0f * minWidth / mBitmap.getWidth();
            }

            if (mBitmap.getWidth() > mScreenwidth) {
                MAX_SCALE = 1;
            } else {
                MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getWidth();
            }
        } else {
            //When the image is higher than wider, MIN_SCALE is calculated according to be
            // minimum 1/8 of the screen width and MAX_SCALE never to exceed screen boundaries
            float minHeight = mScreenwidth / 8;
            if (mBitmap.getHeight() < minHeight) {
                MIN_SCALE = 1f;
            } else {
                MIN_SCALE = 1.0f * minHeight / mBitmap.getHeight();
            }

            if (mBitmap.getHeight() > mScreenwidth) {
                MAX_SCALE = 1;
            } else {
                MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getHeight();
            }
        }

        // Getting the bitmaps for each control button
        topBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_top_enable);
        deleteBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_delete);
        flipVBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_flip);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_resize);

        // Setting the size of the boundaries for each control button
        deleteBitmapWidth = (int) (deleteBitmap.getWidth() * BITMAP_SCALE);
        deleteBitmapHeight = (int) (deleteBitmap.getHeight() * BITMAP_SCALE);

        resizeBitmapWidth = (int) (resizeBitmap.getWidth() * BITMAP_SCALE);
        resizeBitmapHeight = (int) (resizeBitmap.getHeight() * BITMAP_SCALE);

        flipVBitmapWidth = (int) (flipVBitmap.getWidth() * BITMAP_SCALE);
        flipVBitmapHeight = (int) (flipVBitmap.getHeight() * BITMAP_SCALE);

        topBitmapWidth = (int) (topBitmap.getWidth() * BITMAP_SCALE);
        topBitmapHeight = (int) (topBitmap.getHeight() * BITMAP_SCALE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInButton(event, mRectDelete)) {
                    if (operationListener != null) {
                        operationListener.onDeleteClick();
                    }
                } else if (isInResize(event)) {
                    isInResize = true;
                    lastRotateDegree = rotationToStartPoint(event);
                    midPointToStartPoint(event);
                    lastDialognalLength = diagonalLength(event);
                } else if (isInButton(event, mRectFlip)) {
                    mBitmap = flip(mBitmap);
                    isFlipped  = !isFlipped ;
                    invalidate();
                } else if (isInButton(event, mRectTop)) {
                    bringToFront();
                    if (operationListener != null) {
                        operationListener.onTop(this);
                    }
                } else if (isInBitmap(event)) {
                    isInside = true;
                    lastX = event.getX(0);
                    lastY = event.getY(0);
                } else {
                    handled = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:   // If two pointers are detected
                if (spacing(event) > pointerLimitDis) {
                    oldDis = spacing(event);
                    isPointerDown = true;
                    midPointToStartPoint(event);
                } else {
                    isPointerDown = false;
                }
                isInside = false;
                isInResize = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // Pinch zoom - not really good feature
                if (isPointerDown) {
                    float scale;
                    float disNew = spacing(event);
                    if (disNew == 0 || disNew < pointerLimitDis) {
                        scale = 1;
                    } else {
                        scale = disNew / oldDis;
                        // Zoom slowly
                        scale = (scale - 1) * pointerZoomCoeff + 1;
                    }
                    float scaleTemp = (scale * Math.abs(mRectFlip.left - mRectResize.left)) / mOriginalWidth;
                    if (((scaleTemp <= MIN_SCALE)) && scale < 1 ||
                            (scaleTemp >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                    } else {
                        lastDialognalLength = diagonalLength(event);
                    }
                    mMatrix.postScale(scale, scale, mid.x, mid.y);
                    invalidate();

                } else if (isInResize) {
                    mMatrix.postRotate((rotationToStartPoint(event) - lastRotateDegree) * 2, mid.x, mid.y);
                    lastRotateDegree = rotationToStartPoint(event);

                    float scale = diagonalLength(event) / lastDialognalLength;

                    if ((diagonalLength(event) / halfDiagonalLength <= MIN_SCALE) && scale < 1 ||
                            (diagonalLength(event) / halfDiagonalLength >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                        if (!isInResize(event)) {
                            isInResize = false;
                        }
                    } else {
                        lastDialognalLength = diagonalLength(event);
                    }
                    mMatrix.postScale(scale, scale, mid.x, mid.y);
                    invalidate();

                } else if (isInside) {
                    float x = event.getX(0);
                    float y = event.getY(0);
                    //TODO Moving beyond the screen area can not judge
                    mMatrix.postTranslate(x - lastX, y - lastY);
                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isInResize = false;
                isInside = false;
                isPointerDown = false;
                break;

        }
        if (handled && operationListener != null) {
            operationListener.onEdit(this);
        }
        return handled;
    }

    Bitmap flip(Bitmap src) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    /*
    * Check if the pointer is in the bitmap
    * */
    private boolean isInBitmap(MotionEvent event) {
        float[] matrixArray = new float[9];
        mMatrix.getValues(matrixArray);

        // Top left corner
        float topLeftX = 0.0F * matrixArray[0] + 0.0F * matrixArray[1] + matrixArray[2];
        float topLeftY = 0.0F * matrixArray[3] + 0.0F * matrixArray[4] + matrixArray[5];
        // Top right corner
        float topRightX = matrixArray[0] * mBitmap.getWidth() + 0.0F * matrixArray[1] + matrixArray[2];
        float topRightY = matrixArray[3] * mBitmap.getWidth() + 0.0F * matrixArray[4] + matrixArray[5];
        // Bottom left corner
        float bottomLeftX = 0.0F * matrixArray[0] + matrixArray[1] * mBitmap.getHeight() + matrixArray[2];
        float bottomLeftY = 0.0F * matrixArray[3] + matrixArray[4] * mBitmap.getHeight() + matrixArray[5];
        // Bottom right corner
        float bottomRightX = matrixArray[0] * mBitmap.getWidth() + matrixArray[1] * mBitmap.getHeight() + matrixArray[2];
        float bottomRightY = matrixArray[3] * mBitmap.getWidth() + matrixArray[4] * mBitmap.getHeight() + matrixArray[5];

        float[] arrayOfFloatX = new float[4];
        float[] arrayOfFloatY = new float[4];
        // Determining the range of the X-direction
        arrayOfFloatX[0] = topLeftX; // Top left x
        arrayOfFloatX[1] = topRightX; // Top right x
        arrayOfFloatX[2] = bottomRightX; // Bottom right x
        arrayOfFloatX[3] = bottomLeftX; // Bottom left x
        // Determining the rang e of the Y-direction
        arrayOfFloatY[0] = topLeftY; // Top left y
        arrayOfFloatY[1] = topRightY; // Top right y
        arrayOfFloatY[2] = bottomRightY; // Bottom right y
        arrayOfFloatY[3] = bottomLeftY; // Bottom left y
        return pointInRect(arrayOfFloatX, arrayOfFloatY, event.getX(0), event.getY(0));
    }

    /*
    * Determine whether a point inside the rectangle
    * */
    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {
        // The length of the four sides
        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);
        // Measuring point distance to four points
        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;

        //Area of a rectangle
        double s = a1 * a2;
        //Heron`s formula for the all four triangles
        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5; // Max marginal error of 0.5 difference between them
    }

    /*
    * Check is the pointer in rect
    * */
    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    /*
    * Handle larger area
    * */
    private boolean isInResize(MotionEvent event) {
        int left = -20 + mRectResize.left;
        int top = -20 + mRectResize.top;
        int right = 20 + mRectResize.right;
        int bottom = 20 + mRectResize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    /*
    * Midpoint position between top left corner and touch point - used in resizing
    * */
    private void midPointToStartPoint(MotionEvent event) {
        float[] arrayOfFloat = new float[9];
        mMatrix.getValues(arrayOfFloat);
        float f1 = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = f1 + event.getX(0);
        float f4 = f2 + event.getY(0);
        mid.set(f3 / 2, f4 / 2);
    }

    /*
    * Calculating rotation according to upper left corner
    * */
    private float rotationToStartPoint(MotionEvent event) {
        float[] arrayOfFloat = new float[9];
        mMatrix.getValues(arrayOfFloat);
        float x = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float y = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        double arc = Math.atan2(event.getY(0) - y, event.getX(0) - x);
        return (float) Math.toDegrees(arc);
    }

    /*
    * From the rectangle/pointer midpoint to the touch point - used in resizing
    * */
    private float diagonalLength(MotionEvent event) {
        float diagonalLength = (float) Math.hypot(event.getX(0) - mid.x, event.getY(0) - mid.y);
        return diagonalLength;
    }

    /*
    * Calculating the distance between two pointers
    * */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    public interface OperationListener {
        void onDeleteClick();

        void onEdit(StickerView stickerView);

        void onTop(StickerView stickerView);
    }

    public void setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
    }

    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }
}

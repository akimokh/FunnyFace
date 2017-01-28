package com.example.yonko.funnyface.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.yonko.funnyface.R;


public class BubbleTextView extends ImageView {
    private static final String TAG = BubbleTextView.class.getSimpleName();

    private Bitmap mDeleteBitmap;
    private Bitmap mFlipVBitmap;
    private Bitmap mTopBitmap;
    private Bitmap mResizeBitmap;
    private Bitmap mBitmap;
    private Bitmap mOriginBitmap;
    private Rect mRectDelete;
    private Rect mRectResize;
    private Rect mRectFlip;
    private Rect mRectTop;

    private int mDeleteBitmapWidth;
    private int mDeleteBitmapHeightWidth;
    private int mResizeBitmapWidth;
    private int mResizeBitmapHeight;
    private int mFlipVBitmapWidth;
    private int mFlipVBitmapHeight;

    private int mTopBitmapWidth;
    private int mTopBitmapHeight;
    private Paint mLocalPaint;
    private int mScreenwidth, mScreenHeight;
    private static final float BITMAP_SCALE = 0.7f;
    private PointF mid = new PointF();
    private OperationListener mOperationListener;
    private float mLastRotateDegree;

    private boolean isPointerDown = false; // If second pointer is down

    // Maximum distance for pointer moving
    private final float POINTER_LIMIT_DISTANCE = 20f;
    private final float POINTER_ZOOM_COEFFICENT = 0.09f;
    private final float MOVE_LIMIT_DISTANCE = 0.5f;

    private float mLastLength;
    private boolean isInResize = false;

    private Matrix mMatrix = new Matrix();
    private boolean isInSide;

    private float mLastX, mLastY;

    private boolean isInEdit = true;

    // bubble ratio to screen
    private float RATIO_TO_SCREEN = 0.6f;
    private float MIN_SCALE = 0.9f;
    private float MAX_SCALE = 1.2f;

    private double mHalfDiagonalLength;
    private float mOringinWidth = 0;

    private final String DEFAULT_TEXT;
    private String mStr = ""; // Display string

    private final float mDefaultTextSize = 23;
    private float mFontSize = 23;

    // Max and min size
    private final float mMaxFontSize = 16;
    private final float mMinFontSize = 32;

    // Text brush
    private TextPaint mFontPaint;

    private Canvas canvasText;
    private Paint.FontMetrics fm;
    private DisplayMetrics dm;

    private float mBaseline; // Text mBaseline
    boolean isInit = true;

    private float mOldDistance; //Initial distance when pinch to zoom.

    private boolean isDown = false;
    private boolean isMove = false;
    private boolean isUp = false;

    private final int mFontColor;

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DEFAULT_TEXT = getContext().getString(R.string.double_click_input_text);
        this.mFontColor = Color.BLACK;
        init();
    }

    public BubbleTextView(Context context) {
        super(context);
        DEFAULT_TEXT = getContext().getString(R.string.double_click_input_text);
        this.mFontColor = Color.BLACK;
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DEFAULT_TEXT = getContext().getString(R.string.double_click_input_text);
        this.mFontColor = Color.BLACK;
        init();
    }

    public BubbleTextView(Context context, int mFontColor) {
        super(context);
        DEFAULT_TEXT = getContext().getString(R.string.double_click_input_text);
        this.mFontColor = mFontColor;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            float[] arrayOfFloat = new float[9];
            mMatrix.getValues(arrayOfFloat); // Getting the values from transformation matrix
            float topLeftX = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2]; // Transition of x -- global x
            float topLeftY = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5]; // Transition of y -- global y
            float topRightX = arrayOfFloat[0] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[1] + arrayOfFloat[2]; // Top right  x
            float topRightY = arrayOfFloat[3] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[4] + arrayOfFloat[5]; // Top right  y
            float bottomLeftX = 0.0F * arrayOfFloat[0] + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2]; // Bottom left x
            float bottomLeftY = 0.0F * arrayOfFloat[3] + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5]; // Bottom left y
            float bottomRightX = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2]; // Bottom right x
            float bottomRightY = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5]; // Bottom right y

            canvas.save();

            // First is text draw
            mBitmap = mOriginBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvasText.setBitmap(mBitmap);
            canvasText.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, dm);
            float scaleX = arrayOfFloat[Matrix.MSCALE_X];
            float skewY = arrayOfFloat[Matrix.MSKEW_Y];
            float rScale = (float) Math.sqrt(scaleX * scaleX + skewY * skewY);

            float size = rScale * 0.75f * mDefaultTextSize;
            if (size > mMaxFontSize) {
                mFontSize = mMaxFontSize;
            } else if (size < mMinFontSize) {
                mFontSize = mMinFontSize;
            } else {
                mFontSize = size;
            }
            mFontPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mFontSize, dm));
            String[] texts = autoSplit(mStr, mFontPaint, mBitmap.getWidth() - left * 3);
            float height = (texts.length * (mBaseline + fm.leading) + mBaseline);
            float top = (mBitmap.getHeight() - height) / 2;
            // Baseline painting
            top += mBaseline;
            for (String text : texts) {
                if (TextUtils.isEmpty(text)) {
                    continue;
                }
                canvasText.drawText(text, mBitmap.getWidth() / 2, top, mFontPaint);  // In the upper left corner as the origin coordinate control
                top += mBaseline + fm.leading; // Add font line spacing
            }
            canvas.drawBitmap(mBitmap, mMatrix, null);

            // The remove button in the upper right corner
            mRectDelete.left = (int) (topRightX - mDeleteBitmapWidth / 2);
            mRectDelete.right = (int) (topRightX + mDeleteBitmapWidth / 2);
            mRectDelete.top = (int) (topRightY - mDeleteBitmapHeightWidth / 2);
            mRectDelete.bottom = (int) (topRightY + mDeleteBitmapHeightWidth / 2);
            // The stretching button in the lower right corner
            mRectResize.left = (int) (bottomRightX - mResizeBitmapWidth / 2);
            mRectResize.right = (int) (bottomRightX + mResizeBitmapWidth / 2);
            mRectResize.top = (int) (bottomRightY - mResizeBitmapHeight / 2);
            mRectResize.bottom = (int) (bottomRightY + mResizeBitmapHeight / 2);
            // The BringToTop button in the upper left corner
            mRectTop.left = (int) (topLeftX - mTopBitmapWidth / 2);
            mRectTop.right = (int) (topLeftX + mTopBitmapWidth / 2);
            mRectTop.top = (int) (topLeftY - mTopBitmapHeight / 2);
            mRectTop.bottom = (int) (topLeftY + mTopBitmapHeight / 2);

            if (isInEdit) {
                canvas.drawLine(topLeftX, topLeftY, topRightX, topRightY, mLocalPaint);
                canvas.drawLine(topRightX, topRightY, bottomRightX, bottomRightY, mLocalPaint);
                canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY, mLocalPaint);
                canvas.drawLine(bottomLeftX, bottomLeftY, topLeftX, topLeftY, mLocalPaint);

                canvas.drawBitmap(mDeleteBitmap, null, mRectDelete, null);
                canvas.drawBitmap(mResizeBitmap, null, mRectResize, null);
                canvas.drawBitmap(mTopBitmap, null, mRectTop, null);
            }

            canvas.restore();
        }
    }

    private String[] autoSplit(String content, Paint p, float width) {
        int length = content.length();
        float textWidth = p.measureText(content);
        if (textWidth <= width) {
            return new String[]{content};
        }

        int start = 0, end = 1, i = 0;
        int lines = (int) Math.ceil(textWidth / width); // Calculate the number of rows
        String[] lineTexts = new String[lines];
        while (start < length) {
            if (p.measureText(content, start, end) > width) { // When the text is wider than the width of the control
                lineTexts[i++] = (String) content.subSequence(start, end);
                start = end;
            }
            if (end == length) { // Less than one line of text
                lineTexts[i] = (String) content.subSequence(start, end);
                break;
            }
            end += 1;
        }
        return lineTexts;
    }

    private void init() {
        dm = getResources().getDisplayMetrics();
        mRectDelete = new Rect();
        mRectResize = new Rect();
        mRectFlip = new Rect();
        mRectTop = new Rect();
        mLocalPaint = new Paint();
        mLocalPaint.setColor(getResources().getColor(R.color.red_e73a3d));
        mLocalPaint.setAntiAlias(true);
        mLocalPaint.setDither(true);
        mLocalPaint.setStyle(Paint.Style.STROKE);
        mLocalPaint.setStrokeWidth(2.0f);
        mScreenwidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mFontSize = mDefaultTextSize;
        mFontPaint = new TextPaint();
        mFontPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mFontSize, dm));
        mFontPaint.setColor(mFontColor);
        mFontPaint.setTextAlign(Paint.Align.CENTER);
        mFontPaint.setAntiAlias(true);
        fm = mFontPaint.getFontMetrics();

        mBaseline = fm.descent - fm.ascent;
        isInit = true;
        mStr = DEFAULT_TEXT;
    }

    public void setFontColor(int color) {
        mFontPaint.setColor(color);
    }

    public void setText(String text) {
//        if (TextUtils.isEmpty(text)) {
//            mStr = DEFAULT_TEXT;
//            mFontSize = mDefaultTextSize;
//            mMargin = mDefaultMargin;
//        } else {
        mStr = text;
//        }
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        mMatrix.reset();
        // Otherwise, use a copy of the resource file references
        setBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    public void setBitmap(Bitmap bitmap) {
        mFontSize = mDefaultTextSize;
        mOriginBitmap = bitmap;

        int width = mOriginBitmap.getWidth();
        int height = mOriginBitmap.getHeight();

        //Scaling the bitmap
        float boundingX = mScreenwidth * RATIO_TO_SCREEN;
        float boundingY = mScreenHeight * RATIO_TO_SCREEN;
        float xScale = boundingX / width;
        float yScale = boundingY / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Matrix mMatrix = new Matrix();
        mMatrix.postScale(scale, scale);

        mOriginBitmap = Bitmap.createBitmap(mOriginBitmap, 0, 0, width, height, mMatrix, true);
        mBitmap = mOriginBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvasText = new Canvas(mBitmap);

        setDiagonalLength();
        initBitmaps();
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        mOringinWidth = w;
//        float topbarHeight = DensityUtils.dip2px(getContext(), 50);
//        //Y coordinates (top action bar + square diagram)/2
        mMatrix.postTranslate(mScreenwidth / 2 - w / 2, (mScreenwidth) / 2 - h / 2);
        invalidate();
    }

    private void setDiagonalLength() {
        mHalfDiagonalLength = Math.hypot(mBitmap.getWidth(), mBitmap.getHeight()) / 2;
    }

    private void initBitmaps() {
        float minWidth = mScreenwidth / 8;
        if (mBitmap.getWidth() < minWidth) {
            MIN_SCALE = 1f;
        } else {
            MIN_SCALE = 1.0f * minWidth / mBitmap.getWidth();
        }

        if (mBitmap.getWidth() > mScreenwidth) {
            MAX_SCALE = 1;
        } else {
            MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getWidth();
        }
        mTopBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_top_enable);
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_delete);
        mFlipVBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_flip);
        mResizeBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_resize);

        mDeleteBitmapWidth = (int) (mDeleteBitmap.getWidth() * BITMAP_SCALE);
        mDeleteBitmapHeightWidth = (int) (mDeleteBitmap.getHeight() * BITMAP_SCALE);

        mResizeBitmapWidth = (int) (mResizeBitmap.getWidth() * BITMAP_SCALE);
        mResizeBitmapHeight = (int) (mResizeBitmap.getHeight() * BITMAP_SCALE);

        mFlipVBitmapWidth = (int) (mFlipVBitmap.getWidth() * BITMAP_SCALE);
        mFlipVBitmapHeight = (int) (mFlipVBitmap.getHeight() * BITMAP_SCALE);

        mTopBitmapWidth = (int) (mTopBitmap.getWidth() * BITMAP_SCALE);
        mTopBitmapHeight = (int) (mTopBitmap.getHeight() * BITMAP_SCALE);

    }

    private long preClickTime; 
    private final long doubleClickTimeLimit = 200; // Double click limit time

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInButton(event, mRectDelete)) {
                    if (mOperationListener != null) {
                        mOperationListener.onDeleteClick();
                    }
                    isDown = false;
                } else if (isInResize(event)) {
                    isInResize = true;
                    mLastRotateDegree = rotationToStartPoint(event);
                    midPointToStartPoint(event);
                    mLastLength = diagonalLength(event);
                    isDown = false;
                } else if (isInButton(event, mRectFlip)) {
                    PointF localPointF = new PointF();
                    midDiagonalPoint(localPointF);
                    mMatrix.postScale(-1.0F, 1.0F, localPointF.x, localPointF.y);
                    isDown = false;
                    invalidate();
                } else if (isInButton(event, mRectTop)) {
                    bringToFront();
                    if (mOperationListener != null) {
                        mOperationListener.onTop(this);
                    }
                    isDown = false;
                } else if (isInBitmap(event)) {
                    isInSide = true;
                    mLastX = event.getX(0);
                    mLastY = event.getY(0);
                    isDown = true;
                    isMove = false;
                    isPointerDown = false;
                    isUp = false;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - preClickTime > doubleClickTimeLimit) {
                        preClickTime = currentTime;
                    } else {
                        if (isInEdit && mOperationListener != null) {
                            mOperationListener.onClick(this);
                        }
                    }
                } else {
                    handled = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > POINTER_LIMIT_DISTANCE) {
                    mOldDistance = spacing(event);
                    isPointerDown = true;
                    midPointToStartPoint(event);
                } else {
                    isPointerDown = false;
                }
                isInSide = false;
                isInResize = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // Pinch to zoom
                if (isPointerDown) {
                    float scale;
                    float disNew = spacing(event);
                    if (disNew == 0 || disNew < POINTER_LIMIT_DISTANCE) {
                        scale = 1;
                    } else {
                        scale = disNew / mOldDistance;
                        // Zoom slowly
                        scale = (scale - 1) * POINTER_ZOOM_COEFFICENT + 1;
                    }
                    float scaleTemp = (scale * Math.abs(mRectFlip.left - mRectResize.left)) / mOringinWidth;
                    if (((scaleTemp <= MIN_SCALE)) && scale < 1 ||
                            (scaleTemp >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                    } else {
                        mLastLength = diagonalLength(event);
                    }
                    mMatrix.postScale(scale, scale, mid.x, mid.y);
                    invalidate();
                } else if (isInResize) {
                    mMatrix.postRotate((rotationToStartPoint(event) - mLastRotateDegree) * 2, mid.x, mid.y);
                    mLastRotateDegree = rotationToStartPoint(event);

                    float scale = diagonalLength(event) / mLastLength;

                    if (((diagonalLength(event) / mHalfDiagonalLength <= MIN_SCALE)) && scale < 1 ||
                            (diagonalLength(event) / mHalfDiagonalLength >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                        if (!isInResize(event)) {
                            isInResize = false;
                        }
                    } else {
                        mLastLength = diagonalLength(event);
                    }
                    mMatrix.postScale(scale, scale, mid.x, mid.y);

                    invalidate();
                } else if (isInSide) {
                    //TODO Moving beyond the screen area can not judge
                    float x = event.getX(0);
                    float y = event.getY(0);
                    // Analyzing finger shaking distance plus isMove judgment is true as long as moved
                    if (!isMove && Math.abs(x - mLastX) < MOVE_LIMIT_DISTANCE
                            && Math.abs(y - mLastY) < MOVE_LIMIT_DISTANCE) {
                        isMove = false;
                    } else {
                        isMove = true;
                    }
                    if(isMove) {
                        mMatrix.postTranslate(x - mLastX, y - mLastY);
                    }
                    mLastX = x;
                    mLastY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isInResize = false;
                isInSide = false;
                isPointerDown = false;
                isUp = true;
                break;

        }
        if (handled && mOperationListener != null) {
            mOperationListener.onEdit(this);
        }
        return handled;
    }

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
        // Determining the range of the Y-direction
        arrayOfFloatY[0] = topLeftY; // Top left y
        arrayOfFloatY[1] = topRightY; // Top right y
        arrayOfFloatY[2] = bottomRightY; // Bottom right y
        arrayOfFloatY[3] = bottomLeftY; // Bottom left y
        return pointInRect(arrayOfFloatX, arrayOfFloatY, event.getX(0), event.getY(0));
    }

     /*
     * Determine whether a point inside the rectangle
     **/
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

        // Area of the rectangle
        double s = a1 * a2;

        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;
    }

    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private boolean isInResize(MotionEvent event) {
        int left = -20 + this.mRectResize.left;
        int top = -20 + this.mRectResize.top;
        int right = 20 + this.mRectResize.right;
        int bottom = 20 + this.mRectResize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private void midPointToStartPoint(MotionEvent event) {
        float[] arrayOfFloat = new float[9];
        mMatrix.getValues(arrayOfFloat);
        float f1 = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = f1 + event.getX(0);
        float f4 = f2 + event.getY(0);
        mid.set(f3 / 2, f4 / 2);
    }

    private void midDiagonalPoint(PointF paramPointF) {
        float[] arrayOfFloat = new float[9];
        this.mMatrix.getValues(arrayOfFloat);
        float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
        float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
        float f5 = f1 + f3;
        float f6 = f2 + f4;
        paramPointF.set(f5 / 2.0F, f6 / 2.0F);
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

        void onEdit(BubbleTextView bubbleTextView);

        void onClick(BubbleTextView bubbleTextView);

        void onTop(BubbleTextView bubbleTextView);
    }

    public void setOperationListener(OperationListener mOperationListener) {
        this.mOperationListener = mOperationListener;
    }

    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }

    public String getStr() {
        return mStr;
    }
}

package com.example.yonko.funnyface.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PreviewLineView extends View {
    private static final String TAG = PreviewLineView.class.getSimpleName();

    private int layoutWidth = 0;
    private int layoutHeight = 0;
    private int lineColor = Color.WHITE;
    private float strokeWidth = 6;

    Paint paint = new Paint();

    public PreviewLineView(Context context) {
        super(context);
    }

    public PreviewLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(lineColor);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(0, layoutHeight/2, layoutWidth, layoutHeight/2, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.layoutWidth = w;
        this.layoutHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.paint.setStrokeWidth(strokeWidth);
        this.invalidate();
    }

    public float getStrokeWidth() {
        return this.paint.getStrokeWidth();
    }

    public void setLineColor(int color) {
        this.paint.setColor(color);
        this.invalidate();
    }

    public int getLineColor() {
        return this.paint.getColor();
    }
}

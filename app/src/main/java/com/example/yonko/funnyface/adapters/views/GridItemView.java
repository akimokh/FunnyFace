package com.example.yonko.funnyface.adapters.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GridItemView extends ImageView {
    public GridItemView(Context context) {
        super(context);
    }

    public GridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Make the height equivalent to its width
    }
}

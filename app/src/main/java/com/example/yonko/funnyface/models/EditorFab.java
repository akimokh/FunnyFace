package com.example.yonko.funnyface.models;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

public class EditorFab implements Fab{
    private FloatingActionButton mFloatingActionButton;

    public EditorFab(FloatingActionButton floatingActionButton) {
        mFloatingActionButton = floatingActionButton;
    }

    public void setFloatingActionButton(FloatingActionButton floatingActionButton) {
        mFloatingActionButton = floatingActionButton;
    }

    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public boolean isVisible() {
        return mFloatingActionButton != null && mFloatingActionButton.getVisibility() == View.VISIBLE;
    }

    public void show() {
        show(null);
    }

    public void show(Drawable drawable) {
        if(mFloatingActionButton != null) {
            if(drawable != null) {
                mFloatingActionButton.setImageDrawable(drawable);
            }
            if(!isVisible()) {
                mFloatingActionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hide() {
        if(mFloatingActionButton != null && isVisible()) {
            mFloatingActionButton.setVisibility(View.INVISIBLE);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        if(mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(onClickListener);
        }
    }
}

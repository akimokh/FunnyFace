package com.example.yonko.funnyface.models;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

public interface Fab {
    void setFloatingActionButton(FloatingActionButton floatingActionButton);
    FloatingActionButton getFloatingActionButton();
    boolean isVisible();
    void show();
    void hide();
    void setOnClickListener(View.OnClickListener onClickListener);
}

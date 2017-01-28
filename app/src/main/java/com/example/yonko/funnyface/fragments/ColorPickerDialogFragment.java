package com.example.yonko.funnyface.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.widgets.PreviewLineView;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class ColorPickerDialogFragment extends DialogFragment implements ColorPicker.OnColorChangedListener {
    private static final String TAG = ColorPickerDialogFragment.class.getSimpleName();

    public static final String BRUSH_COLOR = "BRUSH_COLOR";
    public static final String BRUSH_SIZE = "BRUSH_SIZE";

    private static final int SEEK_BAR_MIN = 5;
    private static final int SEEK_BAR_MAX = 60;
    private static final int DEFAULT_BRUSH_COLOR = Color.WHITE;
    private static final int DEFAULT_BRUSH_SIZE = 10;
    
    private int mBrushColor;
    private float mBrushSize;

    private ColorPicker mPicker;
    private SVBar mSvBar;
    private OpacityBar mOpacityBar;
    private PreviewLineView mPreviewLine;
    private SeekBar mSeekBar;

    private ColorPickerDialogListener mColorPickerDialogListener;

    public static ColorPickerDialogFragment newInstance(int mBrushColor, float mBrushSize) {
        ColorPickerDialogFragment f = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BRUSH_COLOR, mBrushColor);
        args.putFloat(BRUSH_SIZE, mBrushSize);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBrushColor = getArguments().getInt(BRUSH_COLOR, DEFAULT_BRUSH_COLOR);
        mBrushSize = getArguments().getFloat(BRUSH_SIZE, DEFAULT_BRUSH_SIZE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (mColorPickerDialogListener != null) {
                                    mColorPickerDialogListener.onFinishColorPickerDialog(mPreviewLine.getLineColor(), mPreviewLine.getStrokeWidth());
                                }
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        LayoutInflater i = getActivity().getLayoutInflater();

        View v = i.inflate(R.layout.dialog_brush_picker, null);

        mPicker = (ColorPicker) v.findViewById(R.id.picker);
        mSvBar = (SVBar) v.findViewById(R.id.svbar);
        mOpacityBar = (OpacityBar) v.findViewById(R.id.opacityBar);
        mPreviewLine = (PreviewLineView) v.findViewById(R.id.previewLine);
        mPreviewLine.setLineColor(mBrushColor);
        mPreviewLine.setStrokeWidth(mBrushSize);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        mSeekBar.setMax((SEEK_BAR_MAX - SEEK_BAR_MIN));
        mSeekBar.setProgress((int)mBrushSize - SEEK_BAR_MIN);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar mSeekBar, int i, boolean b) {
                mPreviewLine.setStrokeWidth(SEEK_BAR_MIN + i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar mSeekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar mSeekBar) {
            }
        });

        mPicker.setOldCenterColor(mBrushColor);
        mPicker.addSVBar(mSvBar);
        mPicker.addOpacityBar(mOpacityBar);
        mPicker.setOnColorChangedListener(this);

        b.setView(v);
        return b.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if(dialog != null) {
            dialog.getWindow()
                    .getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    @Override
    public void onColorChanged(int color) {
        mPreviewLine.setLineColor(color);
    }

    public void setColoPickerDialogListener(ColorPickerDialogListener coloPickerDialogListener) {
        mColorPickerDialogListener = coloPickerDialogListener;
    }

    public interface ColorPickerDialogListener {
        void onFinishColorPickerDialog(int color, float mBrushSize);
    }
}

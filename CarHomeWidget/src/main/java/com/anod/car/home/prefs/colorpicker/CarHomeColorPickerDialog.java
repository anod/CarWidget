package com.anod.car.home.prefs.colorpicker;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerPalette;
import com.android.colorpicker.ColorPickerSwatch;
import com.anod.car.home.R;
import com.anod.car.home.utils.AlphaPatternDrawable;
import com.anod.car.home.utils.ColorUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class CarHomeColorPickerDialog extends ColorPickerDialog {

    protected static final String KEY_ALPHA = "alpha";

    public static final int ALPHA_LEVELS = 5;

    public static final int ALPHA_OPAQUE = 255;

    private boolean mAlphaSliderVisible;

    private ColorPickerPalette mPalette;

    private ProgressBar mProgress;

    private ColorPickerPalette mAlpha;

    private int mSelectedAlpha;

    private View mColorsPanel;

    private HexPanel mHexPanel;

    private Button mHexButton;


    public static CarHomeColorPickerDialog newInstance(int selectedColor,
            boolean alphaSliderVisible, Context context) {
        CarHomeColorPickerDialog ret = new CarHomeColorPickerDialog();
        ret.initialize(ColorUtils.colorChoice(context, R.array.color_picker_values), selectedColor,
                alphaSliderVisible);
        return ret;
    }

    public void initialize(int[] colors, int selectedColor, boolean alphaSliderVisible) {
        super.initialize(R.string.color_dialog_title, colors, selectedColor, 5,
                ColorPickerDialog.SIZE_SMALL);
        getArguments().putBoolean(KEY_ALPHA, alphaSliderVisible);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlphaSliderVisible = getArguments().getBoolean(KEY_ALPHA);
        }

        if (savedInstanceState != null) {
            mAlphaSliderVisible = savedInstanceState.getBoolean(KEY_ALPHA);
        }

        mSelectedAlpha = Color.alpha(mSelectedColor);
        mSelectedColor = alphaColor(ALPHA_OPAQUE, mSelectedColor);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.color_dialog_toolbar);
        toolbar.setTitle(R.string.color_dialog_title);

        mHexButton = (Button) toolbar.findViewById(R.id.hex_switch);
        updateHexButton();
        mHexButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHexDialog();
            }
        });

        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, mColorSelectListener);
        mColorsPanel = view.findViewById(R.id.colors_panel);

        mHexPanel = (HexPanel) view.findViewById(R.id.hex_panel);
        mHexPanel.init(getSelectedColor(), mAlphaSliderVisible);
        mHexPanel.hide();

        if (mAlphaSliderVisible) {
            float density = getResources().getDisplayMetrics().density;
            mAlpha = (ColorPickerPalette) view.findViewById(R.id.alpha_picker);
            mAlpha.setBackground(new AlphaPatternDrawable((int) (5 * density)));
            mAlpha.setVisibility(View.VISIBLE);
            mAlpha.init(mSize, ALPHA_LEVELS, mAlphaSelectListener);
        }

        showPaletteView();

        mAlertDialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, mPositiveListener)
                .setNegativeButton(android.R.string.cancel, mNegativeListener)
                .create();

        return mAlertDialog;
    }

    private void updateHexButton() {
        mHexButton.setText("#" + ColorUtils.toHex(getSelectedColor(), mAlphaSliderVisible));
    }

    @Override
    public void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    private void refreshPalette() {
        if (mPalette != null) {
            mPalette.drawPalette(mColors, mSelectedColor);
            if (mAlpha != null) {
                mAlpha.drawPalette(generateAlphaColors(mSelectedColor), getSelectedColor());
            }
        }
    }

    private int alphaColor(int alpha, int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(alpha, r, g, b);
    }

    private int[] generateAlphaColors(int color) {
        int[] colors = new int[ALPHA_LEVELS];
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        int inc = ALPHA_OPAQUE / ALPHA_LEVELS;
        int alpha = 0;
        for (int i = 0; i < ALPHA_LEVELS - 1; i++) {
            colors[i] = Color.argb(alpha, r, g, b);
            alpha += inc;
        }
        colors[ALPHA_LEVELS - 1] = Color.argb(ALPHA_OPAQUE, r, g, b);
        return colors;
    }

    private DialogInterface.OnClickListener mPositiveListener
            = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int color = getSelectedColor();
            if (mHexPanel.isVisible()) {
                color = mHexPanel.getColor(color);
            }
            if (mListener != null) {
                mListener.onColorSelected(color);
            }
            dismiss();
        }
    };

    private DialogInterface.OnClickListener mNegativeListener
            = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    };

    private ColorPickerSwatch.OnColorSelectedListener mColorSelectListener
            = new ColorPickerSwatch.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            if (color != mSelectedColor) {
                mSelectedColor = color;
                // Redraw palette to show checkmark on newly selected color before dismissing.
                mPalette.drawPalette(mColors, mSelectedColor);
                if (mAlpha != null) {
                    mAlpha.drawPalette(generateAlphaColors(mSelectedColor), getSelectedColor());
                }
                updateHexButton();
            }
        }
    };

    private ColorPickerSwatch.OnColorSelectedListener mAlphaSelectListener
            = new ColorPickerSwatch.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            int alpha = Color.alpha(color);
            if (alpha != mSelectedAlpha) {
                mSelectedAlpha = alpha;
                // Redraw palette to show checkmark on newly selected color before dismissing.
                mAlpha.drawPalette(generateAlphaColors(mSelectedColor), getSelectedColor());
                updateHexButton();
            }
        }
    };

    public int getSelectedColor() {
        return alphaColor(mSelectedAlpha, mSelectedColor);
    }

    private void toggleHexDialog() {
        if (mHexPanel.isVisible()) {
            mHexPanel.hide();
            mColorsPanel.setVisibility(View.VISIBLE);
            return;
        }
        mHexPanel.setColor(getSelectedColor());
        mHexPanel.show();
        mColorsPanel.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ALPHA, mAlphaSliderVisible);
    }

}

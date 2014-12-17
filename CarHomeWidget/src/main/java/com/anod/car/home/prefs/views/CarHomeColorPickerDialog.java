package com.anod.car.home.prefs.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerPalette;
import com.android.colorpicker.ColorPickerSwatch;
import com.anod.car.home.R;
import com.anod.car.home.utils.AlphaPatternDrawable;
import com.anod.car.home.utils.ColorUtils;

public class CarHomeColorPickerDialog extends ColorPickerDialog {
    protected static final String KEY_ALPHA = "alpha";
    public static final int ALPHA_LEVELS = 5;
    public static final int ALPHA_OPAQUE = 255;

    private boolean mAlphaSliderVisible;
    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;
    private ColorPickerPalette mAlpha;

    private int mSelectedAlpha;
    private EditText mHexEdit;
    private View mColorsPanel;
    private boolean mHexVisible;

    public static CarHomeColorPickerDialog newInstance(int selectedColor, boolean alphaSliderVisible, Context context) {
        CarHomeColorPickerDialog ret = new CarHomeColorPickerDialog();
        ret.initialize(ColorUtils.colorChoice(context, R.array.color_picker_values), selectedColor, alphaSliderVisible);
        return ret;
    }

    public void initialize(int[] colors, int selectedColor, boolean alphaSliderVisible) {
        super.initialize(R.string.color_dialog_title, colors, selectedColor, 5, ColorPickerDialog.SIZE_SMALL);
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

        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, mColorSelectListener);
        mColorsPanel = view.findViewById(R.id.colors_panel);
        mHexEdit = (EditText) view.findViewById(R.id.hex_edit);
        mHexEdit.setVisibility(View.GONE);

        if (mAlphaSliderVisible) {
            float density = getResources().getDisplayMetrics().density;
            mAlpha = (ColorPickerPalette) view.findViewById(R.id.alpha_picker);
            mAlpha.setBackground(new AlphaPatternDrawable((int)(5 * density)));
            mAlpha.setVisibility(View.VISIBLE);
            mAlpha.init(mSize, ALPHA_LEVELS, mAlphaSelectListener);
        }

        showPaletteView();

        mAlertDialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Dialog)
                .setView(view)
                .setPositiveButton(android.R.string.ok, mPositiveListener)
                .setNegativeButton(android.R.string.cancel, mNegativeListener)
                .create();

        return mAlertDialog;
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
        if (mPalette!= null) {
            mPalette.drawPalette(mColors, mSelectedColor);
            if (mAlpha!=null) {
                mAlpha.drawPalette(generateAlphaColors(mSelectedColor), alphaColor(mSelectedAlpha,mSelectedColor));
            }
        }
    }

    private int alphaColor(int alpha, int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(alpha, r ,g ,b);
    }

    private int[] generateAlphaColors(int color) {
        int[] colors = new int[ALPHA_LEVELS];
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        int inc = ALPHA_OPAQUE /ALPHA_LEVELS;
        int alpha = 0;
        for(int i=0; i<ALPHA_LEVELS-1; i++) {
            colors[i] = Color.argb(alpha, r ,g ,b);
            alpha+=inc;
        }
        colors[ALPHA_LEVELS-1] = Color.argb(ALPHA_OPAQUE, r ,g ,b);
        return colors;
    }

    private DialogInterface.OnClickListener mPositiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int color = alphaColor(mSelectedAlpha,mSelectedColor);
            if (mHexVisible) {
                color = ColorUtils.fromHex(mHexEdit.getText().toString(), mAlphaSliderVisible, color);
            }
            if (mListener != null) {
                mListener.onColorSelected(color);
            }
            dismiss();
        }
    };

    private DialogInterface.OnClickListener mNegativeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    };

    private ColorPickerSwatch.OnColorSelectedListener mColorSelectListener = new ColorPickerSwatch.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            if (color != mSelectedColor) {
                mSelectedColor = color;
                // Redraw palette to show checkmark on newly selected color before dismissing.
                mPalette.drawPalette(mColors, mSelectedColor);
                if (mAlpha!=null) {
                    mAlpha.drawPalette(generateAlphaColors(mSelectedColor), alphaColor(mSelectedAlpha,mSelectedColor));
                }
            }
        }
    };

    private ColorPickerSwatch.OnColorSelectedListener mAlphaSelectListener = new ColorPickerSwatch.OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            int alpha = Color.alpha(color);
            if (alpha != mSelectedAlpha) {
                mSelectedAlpha = alpha;
                // Redraw palette to show checkmark on newly selected color before dismissing.
                mAlpha.drawPalette(generateAlphaColors(mSelectedColor), alphaColor(mSelectedAlpha,mSelectedColor));
            }
        }
    };


	private void toggleHexDialog() {
        if (mHexVisible) {
            mHexVisible = false;
            mColorsPanel.setVisibility(View.VISIBLE);
            mHexEdit.setVisibility(View.GONE);
            return;
        }
        mHexVisible = true;
		InputFilter filter0 = new InputFilter.LengthFilter((mAlphaSliderVisible) ? 8 : 6);
		InputFilter filter1 = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					char ch = source.charAt(i);
					if (Character.isDigit(ch) || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
						return null;
					} else {
						return "";
					}
				}
				return null;
			}
		};

        mHexEdit.setFilters(new InputFilter[] { filter0, filter1 });
        mHexEdit.setText(ColorUtils.toHex(alphaColor(mSelectedAlpha, mSelectedColor), mAlphaSliderVisible));

        mColorsPanel.setVisibility(View.INVISIBLE);
        mHexEdit.setVisibility(View.VISIBLE);
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ALPHA, mAlphaSliderVisible);
    }

}

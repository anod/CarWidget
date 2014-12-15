package com.anod.car.home.prefs.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.ColorUtils;

public class CarHomeColorPickerDialog extends ColorPickerDialog {
    protected static final String KEY_ALPHA = "alpha";
    public static final int ALPHA_LEVELS = 5;

    private boolean mAlphaSliderVisible;
    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;
    private ColorPickerPalette mAlpha;

    private int mSelectedAlpha;

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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, mColorSelectListener);

        if (mAlphaSliderVisible) {
            float density = getResources().getDisplayMetrics().density;
            mAlpha = (ColorPickerPalette) view.findViewById(R.id.alpha_picker);
            mAlpha.setBackground(new AlphaPatternDrawable((int)(5 * density)));
            mAlpha.init(mSize, ALPHA_LEVELS, mAlphaSelectListener);
        }

        showPaletteView();

        mAlertDialog = new AlertDialog.Builder(activity)
                .setTitle(mTitleResId)
                .setView(view)
                .setPositiveButton(android.R.string.ok, mPositiveListener)
                .setNegativeButton(android.R.string.cancel, mNegativeListener)
                .setNeutralButton("#" + getHexText(mSelectedColor), mNeutralListener)
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

        int inc = 255/ALPHA_LEVELS;
        int alpha = 0;
        for(int i=0; i<ALPHA_LEVELS-1; i++) {
            colors[i] = Color.argb(alpha, r ,g ,b);
            alpha+=inc;
        }
        colors[ALPHA_LEVELS-1] = Color.argb(255, r ,g ,b);
        return colors;
    }

    private DialogInterface.OnClickListener mPositiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mListener != null) {
                mListener.onColorSelected(alphaColor(mSelectedAlpha,mSelectedColor));
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

    private DialogInterface.OnClickListener mNeutralListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            showHexDialog();
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

	private String getHexText(int color) {
		String hexStr = String.format("%08X", color);
		if (!mAlphaSliderVisible) {
			hexStr = hexStr.substring(2);
		}
		return hexStr;
	}

	private int parseColorStr(String hexStr, int defColor) {
		int intValue = defColor;
		try {
			intValue = Color.parseColor("#" + hexStr);
		} catch (IllegalArgumentException e) {
			AppLog.d(e.getMessage());
		}
		if (!mAlphaSliderVisible) {
			intValue = (intValue & 0x00FFFFFF) + 0xFF000000;
		}
		return intValue;
	}


    private void onHexColor(int intValue) {
        refreshPalette();
    }

	private void showHexDialog() {
        final Context context = getActivity();
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(R.string.color_dialog_title);
		final int lastColor = mSelectedColor;
		// Set an EditText view to get user input
		final EditText input = new EditText(context);
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

		input.setFilters(new InputFilter[] { filter0, filter1 });
		input.setText(getHexText(lastColor));
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int intValue = parseColorStr(input.getText().toString(), lastColor);
				onHexColor(intValue);
			}

        });

		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ALPHA, mAlphaSliderVisible);
    }

}

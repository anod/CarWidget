package com.anod.car.home.prefs.views;

import afzkl.development.mColorPicker.ColorPickerDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.anod.car.home.R;

public class CarHomeColorPickerDialog extends ColorPickerDialog {
	private Context mContext;
	private ColorPickerDialog mDialog;
	private boolean mAlphaSliderVisible = false;

	public CarHomeColorPickerDialog(Context context, int initialColor, final OnClickListener listner) {
		super(context, initialColor);
		mContext = context;
		mDialog = this;
		setTitle(R.string.color_dialog_title);
		setAlphaSliderVisible(false);

		Button btn1 = (Button) mView.findViewById(R.id.customButton1);
		btn1.setText(R.string.color_dialog_button_ok);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listner.onClick(mDialog, R.id.customButton1);
				mDialog.dismiss();
			}
		});

		Button btn2 = (Button) mView.findViewById(R.id.customButton2);
		btn2.setText(R.string.color_dialog_button_cancel);
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button btn3 = (Button) mView.findViewById(R.id.customButton3);
		btn3.setText("#" + getHexText(initialColor));
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHexDialog();
			}
		});
		btn3.setSingleLine(true);
		btn3.setEms(9);
	}

	@Override
	public void setAlphaSliderVisible(boolean visible) {
		super.setAlphaSliderVisible(visible);
		mAlphaSliderVisible = visible;
		if (visible) {
			Button btn3 = (Button) mView.findViewById(R.id.customButton3);
			btn3.setText("#" + getHexText(getColor()));
		}
	}

	@Override
	public void onColorChanged(int color) {
		super.onColorChanged(color);
		Button btn = (Button) mView.findViewById(R.id.customButton3);
		if (btn != null) {
			btn.setText("#" + getHexText(color));
		}
	}

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
		}
		if (!mAlphaSliderVisible) {
			intValue = (intValue & 0x00FFFFFF) + 0xFF000000;
		}
		return intValue;
	}

	private void showHexDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(R.string.color_dialog_title);
		final int lastColor = getColor();
		// Set an EditText view to get user input
		final EditText input = new EditText(mContext);
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
		Resources r = mContext.getResources();
		String okText = r.getString(R.string.color_dialog_button_ok);
		alert.setPositiveButton(okText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int intValue = parseColorStr(input.getText().toString(), lastColor);
				mNewColor.setColor(intValue);
				mColorPicker.setColor(intValue, true);
			}
		});

		String cancelText = r.getString(R.string.color_dialog_button_cancel);
		alert.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

}

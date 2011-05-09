package com.anod.car.home;

import afzkl.development.mColorPicker.ColorPickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

public class CarHomeColorPickerDialog extends ColorPickerDialog {
	protected CarHomeColorPickerDialog(Context context, int initialColor, OnClickListener listner) {
		super(context, initialColor);
		setTitle(R.string.color_dialog_title);
		setAlphaSliderVisible(true);
		Resources r = context.getResources();
		String okText = r.getString(R.string.color_dialog_button_ok);
		setButton(BUTTON_POSITIVE, okText, listner);
		String cancelText = r.getString(R.string.color_dialog_button_cancel);
		setButton(BUTTON_NEGATIVE, cancelText, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});		
	}

}

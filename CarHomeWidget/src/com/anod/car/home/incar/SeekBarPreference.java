package com.anod.car.home.incar;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String ANDROIDNS="http://schemas.android.com/apk/res/android";
	private SeekBar mSeekBar;
	private TextView mValueText;
	private final Context mContext;

	private final String mDialogMessage, mSuffix;
	private final int mMax;
	private int mValue;

	public SeekBarPreference(Context context, AttributeSet attrs) { 
		super(context,attrs); 
		mContext = context;

		mDialogMessage = attrs.getAttributeValue(ANDROIDNS,"dialogMessage");
		mSuffix = attrs.getAttributeValue(ANDROIDNS,"text");
		mMax = attrs.getAttributeIntValue(ANDROIDNS,"max", 100);
	}

	public void setValue(int value) {
		if (value > mMax) {
			mValue = mMax;
		} else {
			mValue = value;
		}
		if (mSeekBar != null) {
			mSeekBar.setProgress(mValue);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			int value = mSeekBar.getProgress();
			if (callChangeListener(value)) {
				setValue(value);
				persistInt(value);
			}
		}
	}

	@Override 
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6,6,6,6);

		TextView splashText = new TextView(mContext);
		if (mDialogMessage != null) {
			splashText.setText(mDialogMessage);
		}
		layout.addView(splashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		return layout;
	}

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
    }
    
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        mValue = getPersistedInt(defaultValue == null ? 0 : (Integer) defaultValue);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String t = String.valueOf(progress);
        mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//Nothing
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//Nothing
	}


}

package com.anod.car.home.prefs.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.anod.car.home.R;


public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private SeekBar mSeekBar;
	private TextView mSplashText,mValueText,mSuffixView;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private int mMax, mValue = 0;

	public SeekBarPreference(Context context, AttributeSet attrs) { 
		super(context,attrs); 
		mContext = context;

		mDialogMessage = attrs.getAttributeValue(androidns,"dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns,"text");
		mMax = attrs.getAttributeIntValue(androidns,"max", 100);
	}

    public void setValue(int value) {
        if (value > mMax) {
            value = mMax;
        }
        mValue = value;
        if (mSeekBar != null)
        	mSeekBar.setProgress(mValue);
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
		LayoutInflater l = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) l.inflate(R.layout.seek_bar_dialog, null);

		mSplashText = (TextView) layout.findViewById(R.id.splashText);
		if (mDialogMessage != null) {
			mSplashText.setText(mDialogMessage);
		} else {
			mSplashText.setText("");
		}

		mValueText = (EditText) layout.findViewById(R.id.value);

		mSeekBar = (SeekBar) layout.findViewById(R.id.seekBar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		
		mValueText.setText(String.valueOf(mValue));
		mValueText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				String value = mValueText.getText().toString();
				int i = -1;
				if (value != "") {
					try {
						i = Integer.valueOf(value);
					} catch (Exception e) {}
					if (i > mMax) {
						i = mMax;
						mValueText.setText(String.valueOf(mMax));
					}
					if (i != -1) {
						mSeekBar.setProgress(i);
					}
				}
				return false;
			}
		});
		
		mSuffixView = (TextView) layout.findViewById(R.id.suffix);
        if (mSuffix != null) {
        	mSuffixView.setText(mSuffix);
        }
	
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
        if (fromUser) {
        	mValueText.setText(t);
        }
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
    

}

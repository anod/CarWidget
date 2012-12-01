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
import com.anod.car.home.utils.Utils;


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
		LayoutInflater l = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) l.inflate(R.layout.seek_bar_dialog, null);

		TextView splashText = (TextView) layout.findViewById(R.id.splashText);
		if (mDialogMessage != null) {
			splashText.setText(mDialogMessage);
		} else {
			splashText.setText("");
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
				if (!"".equals(value)) {
					try {
						i = Integer.valueOf(value);
					} catch (Exception e) {
						Utils.logd(e.getMessage());
					}
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
		
		TextView suffixView = (TextView) layout.findViewById(R.id.suffix);
        if (mSuffix != null) {
        	suffixView.setText(mSuffix);
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
		// Nothing
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Nothing
	}
    

}

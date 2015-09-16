package com.anod.car.home.prefs.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.utils.AppLog;


public class SeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat implements SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;

    private TextView mValueText;

    public static SeekBarPreferenceDialogFragment newInstance(String key) {
        SeekBarPreferenceDialogFragment fragment = new SeekBarPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }


    public SeekBarDialogPreference getSeekBarDialogPreference() {
        return (SeekBarDialogPreference) getPreference();
    }

    public void setValue(int value) {
        if (value > getSeekBarDialogPreference().getMax()) {
            getSeekBarDialogPreference().setValue(getSeekBarDialogPreference().getMax());
        } else {
            getSeekBarDialogPreference().setValue(value);
        }

        if (mSeekBar != null) {
            mSeekBar.setProgress(getSeekBarDialogPreference().getValue());
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            int value = mSeekBar.getProgress();
            SeekBarDialogPreference preference = getSeekBarDialogPreference();

            if (preference.callChangeListener(value)) {
                setValue(value);
                preference.persistInt(value);
            }
        }
    }

    @Override
    public View onCreateDialogView(Context context) {
        LayoutInflater l = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) l.inflate(R.layout.seek_bar_dialog, null);

        TextView splashText = (TextView) layout.findViewById(R.id.splashText);
        if (getSeekBarDialogPreference().getDialogMessage() != null) {
            splashText.setText(getSeekBarDialogPreference().getDialogMessage());
        } else {
            splashText.setText("");
        }

        final int max = getSeekBarDialogPreference().getMax();
        mValueText = (EditText) layout.findViewById(R.id.value);

        mSeekBar = (SeekBar) layout.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(max);
        mSeekBar.setProgress(getSeekBarDialogPreference().getValue());

        mValueText.setText(String.valueOf(getSeekBarDialogPreference().getValue()));
        mValueText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String value = mValueText.getText().toString();
                int i = -1;
                if (!"".equals(value)) {
                    try {
                        i = Integer.valueOf(value);
                    } catch (Exception e) {
                        AppLog.d(e.getMessage());
                    }
                    if (i > max) {
                        i = max;
                        mValueText.setText(String.valueOf(max));
                    }
                    if (i != -1) {
                        mSeekBar.setProgress(i);
                    }
                }
                return false;
            }
        });

        TextView suffixView = (TextView) layout.findViewById(R.id.suffix);
        if (getSeekBarDialogPreference().getSuffix() != null) {
            suffixView.setText(getSeekBarDialogPreference().getSuffix());
        }

        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(getSeekBarDialogPreference().getMax());
        mSeekBar.setProgress(getSeekBarDialogPreference().getValue());
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

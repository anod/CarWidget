package com.anod.car.home.prefs.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 2015-09-14
 */
public class SeekBarDialogPreference extends DialogPreference {
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";


    private final String mSuffix;

    private final int mMax;

    private int mValue;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSuffix = attrs.getAttributeValue(ANDROIDNS, "text");
        mMax = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
    }


    public int getMax() {
        return mMax;
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public boolean persistInt(int value) {
        return super.persistInt(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        mValue = getPersistedInt(defaultValue == null ? 0 : (Integer) defaultValue);
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
    }
}

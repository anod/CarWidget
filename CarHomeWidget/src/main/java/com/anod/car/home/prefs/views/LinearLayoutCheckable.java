package com.anod.car.home.prefs.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LinearLayoutCheckable extends LinearLayout implements Checkable {

    public LinearLayoutCheckable(Context context) {
        super(context);
    }

    public LinearLayoutCheckable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutCheckable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean isChecked() {
        return isSelected();
    }

    @Override
    public void setChecked(boolean checked) {
        setSelected(true);
    }

    @Override
    public void toggle() {
        setSelected(!isSelected());
    }

}

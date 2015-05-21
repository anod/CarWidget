package com.anod.car.home.prefs.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class RelativeLayoutCheckable extends RelativeLayout implements Checkable {

    private Checkable mCheckableView;

    public RelativeLayoutCheckable(Context context) {
        super(context);
    }

    public RelativeLayoutCheckable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutCheckable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initCheckableView();
    }

    private void initCheckableView() {
        mCheckableView = (Checkable) findViewById(android.R.id.text1);
    }

    @Override
    public boolean isChecked() {
        return mCheckableView.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        mCheckableView.setChecked(checked);
    }

    @Override
    public void toggle() {
        mCheckableView.toggle();
    }

}

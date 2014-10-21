package com.anod.car.home.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 2014-10-20
 */
public class TwoLineButton extends LinearLayout {

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;

    public TwoLineButton(Context context) {
        this(context, null);
    }

    public TwoLineButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLineButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater.from(context).inflate(R.layout.two_line_button, this);

        mIconView = (ImageView)findViewById(android.R.id.icon);
        mTitleView = (TextView)findViewById(android.R.id.title);
        mSummaryView = (TextView)findViewById(android.R.id.summary);


        CharSequence summary=null;
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.TwoLineButtonAttrs, defStyle, 0);
        int n = ta.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.TwoLineButtonAttrs_icon_src:
                    Drawable d = ta.getDrawable(i);
                    if (d != null) {
                        setIcon(d);
                    }
                    break;
                case R.styleable.TwoLineButtonAttrs_title_text:
                    setTitle(ta.getText(i));
                    break;
                case R.styleable.TwoLineButtonAttrs_summary_text:
                    summary = ta.getText(i);
                    break;
            }
        }
        ta.recycle();

        if (summary == null) {
            mSummaryView.setVisibility(View.GONE);
        } else {
            mSummaryView.setText(summary);
        }

    }


    private void setIcon(Drawable drawable) {
        mIconView.setImageDrawable(drawable);
    }

    public void setIcon(int resId) {
        mIconView.setImageResource(resId);
    }

    public void setTitle(CharSequence text) {
        mTitleView.setText(text);
    }

    public void setTitle(int resId) {
        mTitleView.setText(resId);
    }

    public void setSummary(int resId) {
        mSummaryView.setText(resId);
        mSummaryView.setVisibility(View.VISIBLE);
    }

    public void setSummary(CharSequence text) {
        mSummaryView.setText(text);
        mSummaryView.setVisibility(View.VISIBLE);
    }

    public void setSummaryVisibility(int visibility) {
        mSummaryView.setVisibility(visibility);
    }
}

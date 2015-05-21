package com.anod.car.home.prefs.views;

import com.anod.car.home.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class HeaderPreference extends Preference {

    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";

    /**
     * mIconResId is overridden by mIcon, if mIcon is specified.
     */
    private final int mIconResId;

    //private final String mFragment;
    private Drawable mIcon;

    public HeaderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIconResId = attrs.getAttributeResourceValue(ANDROIDNS, "icon", 0);
        //mFragment = attrs.getAttributeValue(ANDROIDNS, "fragment");
        setLayoutResource(R.layout.header_preference);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        if (imageView == null) {
            return;
        }
        if (mIconResId == 0) {
            return;
        }
        if (mIcon == null) {
            mIcon = getContext().getResources().getDrawable(mIconResId);
        }
        if (mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }
        imageView.setVisibility(mIcon != null ? View.VISIBLE : View.GONE);
    }
}

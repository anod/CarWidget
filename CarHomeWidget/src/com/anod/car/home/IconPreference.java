package com.anod.car.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class IconPreference extends Preference {
	private Bitmap mIconBitmap;
    public IconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public IconPreference(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public IconPreference(Context context) {
    	super(context);
    }
    public void setIconBitmap(Bitmap iconBitmap) {
    	mIconBitmap = iconBitmap;
    	notifyChanged();
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ImageView imageView = (ImageView)view.findViewById(R.id.pref_icon_view);
	    if (imageView != null && mIconBitmap != null) {
	    	imageView.setImageBitmap(mIconBitmap);
	    }
    }
}

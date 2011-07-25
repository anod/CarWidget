package com.anod.car.home.prefs.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.anod.car.home.R;

public class IconPreference extends Preference {
	private Bitmap mIconBitmap;
	private ImageButton mEditButton;
    public IconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.pref_icon);
    }
    
    public IconPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_icon);
    }
    
    public IconPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.pref_icon);
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

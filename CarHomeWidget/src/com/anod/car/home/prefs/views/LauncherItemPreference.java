package com.anod.car.home.prefs.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.anod.car.home.R;

public class LauncherItemPreference extends Preference implements OnClickListener {
	private static final int INVALID_RESOURCE = 0;
	private Bitmap mIconBitmap;
	private int mIconResource = INVALID_RESOURCE;
	private OnPreferenceClickListener mDeleteClickListener;
	private Boolean mShowEditButton = false;
	private int mCellId;
	
    public LauncherItemPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.pref_icon);
    }
    
    public LauncherItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_icon);
    }
    
    public LauncherItemPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.pref_icon);
    }
    
    
    public void setCellId(int cellId) {
    	mCellId = cellId;
    }
    
    public int getCellId() {
    	return mCellId;
    }
    
    public void showButtons(boolean show) {
    	mShowEditButton = show;
    	notifyChanged();
    }
    
    public void setIconResource(int resId) {
    	mIconBitmap = null;
    	mIconResource = resId;
    	notifyChanged();
    }
    
    public void setIconBitmap(Bitmap iconBitmap) {
    	mIconBitmap = iconBitmap;
    	mIconResource = INVALID_RESOURCE;
    	notifyChanged();
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ImageView imageView = (ImageView)view.findViewById(R.id.pref_icon_view);
	    if (imageView != null && mIconBitmap != null) { 
	    	imageView.setImageBitmap(mIconBitmap);
	    } 
	    if (imageView != null && mIconResource > 0) {
	    	imageView.setImageResource(mIconResource);
	    }
	    
        ImageView editButton = (ImageView)view.findViewById(R.id.delete_action_button);
        ImageView replaceImage = (ImageView)view.findViewById(R.id.edit_icon);
        View divider = (View)view.findViewById(R.id.divider);
	    if (mShowEditButton) {
	    	editButton.setOnClickListener(this);
	    	editButton.setVisibility(View.VISIBLE);
	    	divider.setVisibility(View.VISIBLE);
	    	replaceImage.setVisibility(View.VISIBLE);
	    } else {
	    	editButton.setVisibility(View.GONE);
	    	divider.setVisibility(View.GONE);
	    	replaceImage.setVisibility(View.GONE);
	    }
    }

	@Override
	public void onClick(View v) {
		if (mDeleteClickListener != null) {
			mDeleteClickListener.onPreferenceClick(this);
		}
	}
	
	public void setOnDeleteClickListener(OnPreferenceClickListener listener) {
		mDeleteClickListener = listener;
	}
}

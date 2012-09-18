package com.anod.car.home.utils;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anod.car.home.R;

public class TitleBarUtils {

	private Activity mActivity;

	public TitleBarUtils(Activity activity) {
		mActivity = activity;
	}

	public void setCustomTitleBar() {
		mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title);
	}

	public LinearLayout getTitleBar() {
		return (LinearLayout) mActivity.findViewById(R.id.actionbar_compat);
	}

	/**
	 * Sets up the compatibility action bar with the given title.
	 */
	public void setupActionBar() {

		LinearLayout titleBar = getTitleBar();

		LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
		springLayoutParams.weight = 1;

		// Add title text
		TextView titleText = new TextView(mActivity, null, R.attr.actionbarCompatTitleStyle);
		titleText.setLayoutParams(springLayoutParams);
		titleText.setText(mActivity.getTitle());
		titleBar.addView(titleText);
	}
}

package com.anod.car.home.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.prefs.preferences.AppTheme;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 11/20/13
 */
abstract public class CarWidgetActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int theme = getApp().getThemeIdx();
		int themeRes = (isTransparentAppTheme()) ? AppTheme.getTransparentResource(theme) : AppTheme.getMainResource(theme);
		setTheme(themeRes);

		super.onCreate(savedInstanceState);

		//
	}

	protected boolean isTransparentAppTheme() {
		return false;
	}

	public CarWidgetApplication getApp() {
		return ((CarWidgetApplication)getApplication());
	}

	// Backwards compatible recreate().
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void recreate()
	{
		if (Utils.IS_HONEYCOMB_OR_GREATER)
		{
			super.recreate();
		}
		else
		{
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}
}

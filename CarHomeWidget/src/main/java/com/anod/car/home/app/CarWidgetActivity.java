package com.anod.car.home.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.prefs.preferences.AppTheme;

/**
 * @author alex
 * @date 11/20/13
 */
abstract public class CarWidgetActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int theme = getApp().getThemeIdx();
		int themeRes = (isTransparentAppTheme()) ? AppTheme.getTransparentResource(theme) : AppTheme.getMainResource(theme);
		setTheme(themeRes);

		//
	}

	protected boolean isTransparentAppTheme() {
		return false;
	}

	protected CarWidgetApplication getApp() {
		return ((CarWidgetApplication)getApplication());
	}

}

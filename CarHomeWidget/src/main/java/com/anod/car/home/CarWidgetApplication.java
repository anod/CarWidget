package com.anod.car.home;

import android.app.Application;
import android.content.Context;

import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.AppTheme;

public class CarWidgetApplication extends Application {
	public AppsList mAppListCache;
	public AppsList mIconThemesCache;

	private int mThemeIdx;

	public static CarWidgetApplication getApplication(Context context) {
		return (CarWidgetApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		//ACRA.init(this);

		mThemeIdx = AppTheme.getTheme(this);

	}


	public AppsList getAppListCache() {
		return mAppListCache;
	}


	public int getThemeIdx() {
		return mThemeIdx;
	}

	public int setThemeIdx(int theme) {
		return mThemeIdx = theme;
	}

	public void initAppListCache() {
		if (mAppListCache == null) {
			mAppListCache = new AppsList(this);
		}
	}

	public AppsList getIconThemesCache() {
		return mIconThemesCache;
	}

	public void initIconThemesCache() {
		if (mIconThemesCache == null) {
			mIconThemesCache = new AppsList(this);
		}
	}

}

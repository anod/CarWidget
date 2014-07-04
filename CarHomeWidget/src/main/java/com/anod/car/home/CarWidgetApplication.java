package com.anod.car.home;

import android.app.Application;
import android.content.Context;

import com.anod.car.home.model.AppsListCache;
import com.anod.car.home.prefs.preferences.AppTheme;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;

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

        Tracker t = GoogleAnalytics.getInstance(this).newTracker(R.xml.tracker_config);
	}


	public AppsListCache getAppListCache() {
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
			mAppListCache = new AppsListCache(this);
		}
	}

	public AppsListCache getIconThemesCache() {
		return mIconThemesCache;
	}

	public void initIconThemesCache() {
		if (mIconThemesCache == null) {
			mIconThemesCache = new AppsListCache(this);
		}
	}

}

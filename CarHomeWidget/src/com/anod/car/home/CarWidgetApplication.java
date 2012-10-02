package com.anod.car.home;

import com.anod.car.home.model.AppsListCache;

import android.app.Application;

public class CarWidgetApplication extends Application {
	public AppsListCache mAppListCache;
	public AppsListCache mIconThemesCache;

    @Override
    public void onCreate() {
        super.onCreate();
    }

	public AppsListCache getAppListCache() {
		return mAppListCache;
	}

	public void initAppListCache() {
		if (mAppListCache == null) {
			mAppListCache = new AppsListCache(this);
		}
	}
    
}

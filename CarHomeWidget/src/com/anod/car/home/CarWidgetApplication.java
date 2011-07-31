package com.anod.car.home;

import com.anod.car.home.model.AllAppsListCache;

import android.app.Application;

public class CarWidgetApplication extends Application {
	public AllAppsListCache mAllAppCache;

    @Override
    public void onCreate() {
        super.onCreate();

        mAllAppCache = new AllAppsListCache(this);
    }

	public AllAppsListCache getAllAppCache() {
		return mAllAppCache;
	}

	public void setAllAppCache(AllAppsListCache mAllAppCache) {
		this.mAllAppCache = mAllAppCache;
	}
    
}

package com.anod.car.home;

import android.app.Application;

public class CarWidgetApplication extends Application {
	public AllAppsListCache mAllAppCache;

    @Override
    public void onCreate() {
        super.onCreate();

        mAllAppCache = new AllAppsListCache(this);
    }
    
}

package com.anod.car.home;

import android.app.Application;

import com.anod.car.home.prefs.model.AppSettings;
import com.anod.car.home.utils.AppLog;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class CarWidgetApplication extends Application {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //LeakCanary.install(this);

        mThemeIdx = AppSettings.create(this).getTheme();
        mObjectGraph = new ObjectGraph(this);
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }


    public int getThemeIdx() {
        return mThemeIdx;
    }

    public int setThemeIdx(int theme) {
        return mThemeIdx = theme;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AppLog.w("Level: " + level);
    }
}
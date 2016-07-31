package com.anod.car.home;

import android.app.Application;
import android.os.Build;

import com.anod.car.home.prefs.model.AppSettings;

import com.crashlytics.android.Crashlytics;

import info.anodsplace.android.log.AppLog;
import io.fabric.sdk.android.Fabric;

public class CarWidgetApplication extends Application implements AppLog.Listener {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AppLog.setDebug(BuildConfig.DEBUG, "CarWidget");
        AppLog.instance().setListener(this);

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

    @Override
    public void onLogException(Throwable tr) {
        Crashlytics.logException(tr);
    }
}
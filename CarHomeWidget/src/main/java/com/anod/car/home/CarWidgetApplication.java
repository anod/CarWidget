package com.anod.car.home;

import com.anod.car.home.prefs.preferences.AppTheme;

import android.app.Application;
import android.content.Context;

public class CarWidgetApplication extends Application {

    private int mThemeIdx;

    private ObjectGraph mObjectGraph;

    public static CarWidgetApplication get(Context context) {
        return (CarWidgetApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);

        mThemeIdx = AppTheme.getTheme(this);
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

    public static ObjectGraph provide(Context context) {
        return ((CarWidgetApplication) context.getApplicationContext()).getObjectGraph();
    }
}
package com.anod.car.home;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.anod.car.home.incar.Handler;
import com.anod.car.home.incar.ModePhoneStateListener;
import com.anod.car.home.incar.ScreenOrientation;
import com.anod.car.home.model.AppsList;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * @author alex
 * @date 2014-10-27
 */
public class ObjectGraph {
    private final CarWidgetApplication app;
    private AppsList mAppListCache;
    private AppsList mIconThemesCache;

    public ObjectGraph(CarWidgetApplication application) {
        this.app = application;
    }

    public Context getApplicationContext() {
        return app.getApplicationContext();
    }

    public CarWidgetApplication getApplication() {
        return app;
    }

    public WindowManager getWindowManager() {
        return (WindowManager) app.getSystemService(WINDOW_SERVICE);
    }

    public TelephonyManager getTelephonyManager() {
        return  (TelephonyManager) app.getSystemService(TELEPHONY_SERVICE);
    }

    public AudioManager getAudioManager() {
        return (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    }

    public ScreenOrientation getScreenOrientation() {
        return new ScreenOrientation(this.app, getWindowManager());
    }

    public ModePhoneStateListener getModePhoneStateListener() {
        return new ModePhoneStateListener(this.app, getAudioManager());
    }

    public Handler getHandler() {
        return new Handler(this.app, getScreenOrientation());
    }

    public AppsList getAppListCache() {
        if (mAppListCache == null) {
            mAppListCache = new AppsList(this.app);
        }
        return mAppListCache;
    }

    public void cleanAppListCache() {
        if (mAppListCache != null) {
            mAppListCache.flush();
            mAppListCache = null;
        }
        if (mIconThemesCache != null) {
            mIconThemesCache.flush();
            mIconThemesCache = null;
        }
    }

    public AppsList getIconThemesCache() {
        if (mIconThemesCache == null) {
            mIconThemesCache = new AppsList(this.app);
        }
        return mIconThemesCache;
    }


}

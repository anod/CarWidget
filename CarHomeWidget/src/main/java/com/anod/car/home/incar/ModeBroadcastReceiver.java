package com.anod.car.home.incar;

import com.anod.car.home.utils.AppLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ModeBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_ACTIVITY_RECOGNITION
            = "com.anod.car.home.incar.ACTION_ACTIVITY_RECOGNITION";

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private final static Object sLock = new Object();

    private static ModeBroadcastReceiver sInstance;

    public static ModeBroadcastReceiver getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ModeBroadcastReceiver();
            }
            return sInstance;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        AppLog.d(" Action: " + act);

        BroadcastService.startService(context);

        ModeDetector.onBroadcastReceive(context, intent);
    }

}

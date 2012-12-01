package com.anod.car.home.incar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anod.car.home.utils.Utils;

public class ModeBroadcastReceiver extends BroadcastReceiver {
    /**
     * Lock used when maintaining queue of requested updates.
     */
	private static Object sLock = new Object();
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
		Utils.logd(" Action: " + intent.getAction());
		Handler.onBroadcastReceive(context, intent);
	}

}

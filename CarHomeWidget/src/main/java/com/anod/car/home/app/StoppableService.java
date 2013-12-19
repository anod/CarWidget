package com.anod.car.home.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.anod.car.home.utils.AppLog;

/**
 * @author alex
 * @date 12/19/13
 */
abstract public class StoppableService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * send this when external code wants the Service to stop
	 */
	public static final String STOP_INTENT_KEY = "STOP_INTENT_KEY";


	public static void fillStopIntent(Intent intent)
	{
		intent.putExtra(STOP_INTENT_KEY, true);
	}
	/**
	 * stop or start an mActivator based on the mActivator type and if an
	 * mActivator is currently running
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent != null)
		{
			if (intent.hasExtra(STOP_INTENT_KEY))
			{
				AppLog.d("Stop service intent");
				onBeforeStop(intent);
				stopSelf();
			} else {
				AppLog.d("Start service intent");
				onAfterStart(intent);
			}
		}

		// restart in case the Service gets canceled
		return START_REDELIVER_INTENT;
	}

	protected abstract void onBeforeStop(Intent intent);
	protected abstract void onAfterStart(Intent intent);

}

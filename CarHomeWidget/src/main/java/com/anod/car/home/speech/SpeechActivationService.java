/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anod.car.home.speech;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.anod.car.home.R;
import com.anod.car.home.app.StoppableService;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.utils.AppLog;

import root.gast.speech.activation.SpeechActivationListener;

/**
 * Persistently run a speech mActivator in the background.
 * Use {@link Intent}s to start and stop it
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 * 
 */
public class SpeechActivationService extends StoppableService implements SpeechActivationListener
{
    private static final String TAG = "SpeechActivationService";
    public static final String ACTIVATION_RESULT_INTENT_KEY = "ACTIVATION_RESULT_INTENT_KEY";
    public static final String ACTIVATION_RESULT_BROADCAST_NAME = "com.anod.car.home.speech.ACTIVATION";
	public static final long DELAY_MILLIS = 2000L;

    public static final int NOTIFICATION_ID = 10298;

    private HotWordActivator mActivator;
	private Handler mEnvHandler;
	private CheckEnvironmentRunnable mEnvRunnable;
	private boolean isStarted = false;

	@Override
    public void onCreate()
    {
        super.onCreate();
    }

    public static Intent makeStartIntent(Context context)
    {
        Intent i = new Intent(context, SpeechActivationService.class);
        return i;
    }

    public static Intent makeStopIntent(Context context)
    {
        Intent i = new Intent(context, SpeechActivationService.class);
		fillStopIntent(i);
        return i;
    }

	@Override
	protected void onBeforeStop(Intent intent) {
		
		stopActivator();
	}

	@Override
	protected void onAfterStart(Intent intent) {
		if (!ModeService.sInCarMode) {
			stopSelf();
			return;
		}
		if (!isStarted) {
			if (mActivator == null)
			{
				AppLog.d("mActivator = null");
				mActivator = new HotWordActivator(this, this, "Okay Widget", "Ok widget", "ok VJ", "Okay VJ");
			}

			if (mActivator.canStartHotword())
			{
				startDetection();
			} else {
				AppLog.d("Delayed start " + mActivator.getClass().getSimpleName());
			}

			mEnvHandler = new Handler();
			if (mEnvRunnable == null) {
				mEnvRunnable = new CheckEnvironmentRunnable(this, mActivator, mEnvHandler);
				mEnvHandler.postDelayed(mEnvRunnable, DELAY_MILLIS);
			}
			isStarted = true;
		}
	}

    @Override
    public void activated(boolean success)
    {
		mEnvHandler.removeCallbacks(mEnvRunnable);
		mEnvRunnable = null;
        // broadcast result
        Intent intent = new Intent(ACTIVATION_RESULT_BROADCAST_NAME);
        intent.putExtra(ACTIVATION_RESULT_INTENT_KEY, success);
        sendBroadcast(intent);

		stopSelf();
    }

    @Override
    public void onDestroy()
    {
		AppLog.d("On destroy");
        super.onDestroy();
        stopActivator();
        stopForeground(true);
    }

    private void stopActivator()
    {
        if (mActivator != null && mActivator.isActive())
        {
            AppLog.d("stopped: " + mActivator.getClass().getSimpleName());
            mActivator.stop();
        }
		isStarted = false;
    }

    @SuppressLint("NewApi")
    public static Notification createNotification(Context context)
    {
        // determine label based on the class
        String name = "Okay Widget";
        String message = context.getString(R.string.speech_activation_notification_listening) + " " + name;
        String title = context.getString(R.string.speech_activation_notification_title);

        PendingIntent pi = PendingIntent.getService(context, 0, makeStopIntent(context), 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.ic_stat_ic_action_mic)
				.setWhen(System.currentTimeMillis())
				.setTicker(message)
				.setContentTitle(title)
				.setContentText(message)
				.setContentIntent(pi)
		;
		Notification notification = builder.build();

        return notification;
    }

	public void startDetection() {
		AppLog.d("Started " + mActivator.getClass().getSimpleName());
		mActivator.detectActivation();
		startForeground(NOTIFICATION_ID, createNotification(this));
	}

	public void stopDetection() {
		mActivator.stop();
	}

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

	final static class CheckEnvironmentRunnable implements Runnable
	{
		private final SpeechActivationService mService;
		private final Handler mHandler;
		private final HotWordActivator mActivator;

		CheckEnvironmentRunnable(SpeechActivationService service, HotWordActivator activator, Handler handler)
		{
			mService = service;
			mActivator = activator;
			mHandler = handler;
		}

		public final void run()
		{
			if (!ModeService.sInCarMode) {
				AppLog.d("CheckEnvironmentRunnable : STOP : !ModeService.sInCarMode");
				return;
			}

			if (mActivator.isMusicActive())
			{

				AppLog.d("CheckEnvironmentRunnable : PAUSE : mActivator.isMusicActive");
				mService.stopDetection();

			} else if (!mActivator.isActive()) {

				if (mActivator.canStartHotword()) {
					AppLog.d("CheckEnvironmentRunnable : START : mActivator.isMusicActive");
					mService.startDetection();
				} else {
					AppLog.d("CheckEnvironmentRunnable : START : !mActivator.canStartHotword");
				}

			} else {
				AppLog.d("CheckEnvironmentRunnable : IDLE : already active");
			}
			mHandler.postDelayed(this, DELAY_MILLIS);
		}

	}


}
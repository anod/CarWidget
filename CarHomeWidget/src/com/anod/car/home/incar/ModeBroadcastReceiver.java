package com.anod.car.home.incar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.anod.car.home.R;
import com.anod.car.home.utils.Utils;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ModeBroadcastReceiver extends BroadcastReceiver {
	public static final String ACTION_ACTIVITY_RECOGNITION = "com.anod.car.home.incar.ACTION_ACTIVITY_RECOGNITION";
	public static final String ACTION_UPDATE_ACTIVITY_CLIENT = "com.anod.car.home.incar.ACTION_UPDATE_ACTIVITY_CLIENT";
	public static final String EXTRA_STATUS = "extra_status";
	/**
     * Lock used when maintaining queue of requested updates.
     */
	private static Object sLock = new Object();
	private static ModeBroadcastReceiver sInstance;
	private UpdateActivityClientListener mUpdateClientListener;

	public static ModeBroadcastReceiver getInstance() {
		synchronized (sLock) {
			if (sInstance == null) {
				sInstance = new ModeBroadcastReceiver();
			}
			return sInstance;
		}
	}

	public interface UpdateActivityClientListener {
		void onUpdate(boolean enable);
	}

	public void setUpdateClientListener(UpdateActivityClientListener listener) {
		mUpdateClientListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String act = intent.getAction();
		Utils.logd(" Action: " + act);

		if (mUpdateClientListener != null && act.equals(ACTION_UPDATE_ACTIVITY_CLIENT)) {
			mUpdateClientListener.onUpdate(intent.getBooleanExtra(EXTRA_STATUS, false));
			return;
		}

		if (act.equals(ACTION_ACTIVITY_RECOGNITION)) {
			ActivityRecognitionResult result = (ActivityRecognitionResult)intent.getExtras().get(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT);
			DetectedActivity probActivity = result.getMostProbableActivity();
			Utils.logd( "Detected activity: [" + String.format("%03d", probActivity.getConfidence()) + "] " + renderActivityType(probActivity.getType()));

			Notification noti = new NotificationCompat.Builder(context)
					.setContentTitle("Activity")
					.setContentText("[" + String.format("%03d", probActivity.getConfidence()) + "] " + renderActivityType(probActivity.getType()))
					.setSmallIcon(R.drawable.ic_launcher_application)
					.build();
			NotificationManager mNotificationManager =
					(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(122, noti);
		}
		Handler.onBroadcastReceive(context, intent);
	}

	private String renderActivityType(int type) {
		if (type == DetectedActivity.IN_VEHICLE) {
			return "IN_VEHICLE";
		}
		if (type == DetectedActivity.ON_BICYCLE) {
			return "ON_BICYCLE";
		}
		if (type == DetectedActivity.ON_FOOT) {
			return "ON_FOOT";
		}
		if (type == DetectedActivity.STILL) {
			return "STILL (NOT MOOVING)";
		}
		if (type == DetectedActivity.TILTING) {
			return "TILTING";
		}
		return "UNKNOWN (" + type +")";
	}


}

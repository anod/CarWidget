package com.anod.car.home.incar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.anod.car.home.R;
import com.anod.car.home.utils.Utils;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ModeBroadcastReceiver extends BroadcastReceiver {
	public static final String ACTION_ACTIVITY_RECOGNITION = "com.anod.car.home.incar.ACTION_ACTIVITY_RECOGNITION";
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

		if (intent.getAction().equals(ACTION_ACTIVITY_RECOGNITION)) {
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

	private boolean isRelevantActivityType(int type) {
		if (type == DetectedActivity.IN_VEHICLE) {
			return true;
		}
		if (type == DetectedActivity.ON_FOOT) {
			return true;
		}
		return false;
		/*
		if (type == DetectedActivity.ON_BICYCLE) {
			return false;
		}
		if (type == DetectedActivity.STILL) {
			return false;
		}
		if (type == DetectedActivity.TILTING) {
			return false;
		}
		return false;;
		*/
	}
}

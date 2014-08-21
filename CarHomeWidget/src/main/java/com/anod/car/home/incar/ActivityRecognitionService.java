package com.anod.car.home.incar;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.R;
import com.anod.car.home.utils.AppLog;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * @author alex
 * @date 6/3/13
 */
public class ActivityRecognitionService extends IntentService {
	/**
	 * Lock used when maintaining queue of requested updates.
	 */
	private final static Object sLock = new Object();

	public static final int MIN_CONFIDENCE = 40;
	public static final int MIN_VEHICLE_CONFIDENCE = 89;
	private static int sLastResult = -1;


	public static void resetLastResult() {
		synchronized (sLock) {
			sLastResult = -1;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public ActivityRecognitionService() {
		super("ActivityRecognitionService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

			if (BuildConfig.DEBUG) {
				DetectedActivity probActivity = result.getMostProbableActivity();
				AppLog.d("Activity: [" + String.format("%03d", probActivity.getConfidence()) + "] " + renderActivityType(probActivity.getType()));

                Notification noti = new NotificationCompat.Builder(this)
                        .setContentTitle("Activity")
                        .setContentText("[" + String.format("%03d", probActivity.getConfidence()) + "] " + renderActivityType(probActivity.getType()))
                        .setSmallIcon(R.drawable.ic_launcher_application)
                        .setTicker("[" + String.format("%03d", probActivity.getConfidence()) + "] " + renderActivityType(probActivity.getType()))
                        .build();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(122, noti);
			}

			DetectedActivity probActivity = result.getMostProbableActivity();
			int conf = probActivity.getConfidence();
			if (probActivity.getConfidence() < MIN_CONFIDENCE) {
				return;
			}
			int type = probActivity.getType();
			if (type == DetectedActivity.IN_VEHICLE && conf < MIN_VEHICLE_CONFIDENCE) {
				return;
			}
			if (type == DetectedActivity.ON_FOOT || type == DetectedActivity.IN_VEHICLE) {
				synchronized (sLock) {
					if (sLastResult != type) {
						sLastResult = type;
						Intent broadcast = new Intent(ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION);
						broadcast.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);
						sendBroadcast(broadcast);
					}
				}
			}

		} else {
            AppLog.d("ActivityRecognitionResult: No Result");
        }
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
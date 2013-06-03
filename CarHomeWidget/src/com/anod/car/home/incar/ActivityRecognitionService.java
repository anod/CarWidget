package com.anod.car.home.incar;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * @author alex
 * @date 6/3/13
 */
public class ActivityRecognitionService extends IntentService {


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
			Intent broadcast = new Intent(ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION);
			//DetectedActivity probActivity = result.getMostProbableActivity();

			broadcast.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);
			sendBroadcast(broadcast);
		}
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
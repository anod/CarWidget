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
	public static final int MIN_CONFIDENCE = 10;
	private static int sLastResult = -1;

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

			DetectedActivity probActivity = result.getMostProbableActivity();
			if (probActivity.getConfidence() < MIN_CONFIDENCE) {
				return;
			}
			int type = probActivity.getType();
			if (type == DetectedActivity.ON_FOOT || type == DetectedActivity.IN_VEHICLE) {
				if (sLastResult != type) {
					sLastResult = type;
					Intent broadcast = new Intent(ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION);
					broadcast.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);
					sendBroadcast(broadcast);
				}
			}

		}
	}

}
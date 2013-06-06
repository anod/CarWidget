package com.anod.car.home.incar;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
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
			broadcast.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);
			sendBroadcast(broadcast);
		}
	}

}
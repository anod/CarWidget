package com.anod.car.home.incar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.app.StoppableService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * @author alex
 * @date 12/25/13
 */
public class ActivityRecognitionClientService extends StoppableService  implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private ActivityRecognitionClient mActivityRecognitionClient;

	// Constants used to establish the activity update interval
	public static final int MILLISECONDS_PER_SECOND = 1000;

	public static final int DETECTION_INTERVAL_SECONDS = 10;

	public static final int DETECTION_INTERVAL_MILLISECONDS =
			MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;


	public static Intent makeStartIntent(Context context)
	{
		Intent i = new Intent(context.getApplicationContext(), ActivityRecognitionClientService.class);
		return i;
	}

	public static Intent makeStopIntent(Context context)
	{
		Intent i = new Intent(context.getApplicationContext(), ActivityRecognitionClientService.class);
		fillStopIntent(i);
		return i;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onBeforeStop(Intent intent) {
		detachActivityRecognitionClient();
	}

	@Override
	protected void onAfterStart(Intent intent) {
		attachActivityRecognitionClient();
	}


	@Override
	public void onDestroy() {
		detachActivityRecognitionClient();
		super.onDestroy();
	}

	private void attachActivityRecognitionClient() {
		if (mActivityRecognitionClient == null) {
			// Connect to the ActivityRecognitionService
			mActivityRecognitionClient = new ActivityRecognitionClient(getApplicationContext(), this, this);
			mActivityRecognitionClient.connect();
		}
	}

	private void detachActivityRecognitionClient() {
		if (mActivityRecognitionClient != null) {
			mActivityRecognitionClient.removeActivityUpdates(getActivityRecognitionPendingIntent());
			mActivityRecognitionClient.disconnect();
			mActivityRecognitionClient = null;
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "Activity Recognition Client Connected", Toast.LENGTH_SHORT).show();
		}
		// 3 sec
		mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, getActivityRecognitionPendingIntent());
	}

	private PendingIntent getActivityRecognitionPendingIntent() {
		Intent intent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onDisconnected() {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "Activity Recognition Client Disconnected",Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "Activity Recognition Client Connection Failed: "+connectionResult.toString(),Toast.LENGTH_SHORT).show();
		}
	}

}

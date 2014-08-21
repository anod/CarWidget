package com.anod.car.home.incar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.app.StoppableService;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * @author alex
 * @date 12/25/13
 */
public class ActivityRecognitionClientService extends StoppableService  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private GoogleApiClient mGoogleApiClient;

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
		if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
		}
        mGoogleApiClient.connect();
	}

	private void detachActivityRecognitionClient() {
		if (mGoogleApiClient != null) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,getActivityRecognitionPendingIntent());
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "GoogleApiClient Connected", Toast.LENGTH_SHORT).show();
            AppLog.d("GoogleApiClient: Connected");
		}

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,DETECTION_INTERVAL_MILLISECONDS,getActivityRecognitionPendingIntent())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (BuildConfig.DEBUG) {
                                AppLog.d("requestActivityUpdates: Success");
                            }
                        } else {
                            if (BuildConfig.DEBUG) {
                                AppLog.d("requestActivityUpdates: Error: "+status.getStatusMessage());
                            }
                        }
                    }
                });
	}

    @Override
    public void onConnectionSuspended(int i) {

    }

    private PendingIntent getActivityRecognitionPendingIntent() {
		Intent intent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "GoogleApiClient Connection Failed: "+connectionResult.toString(),Toast.LENGTH_SHORT).show();
            AppLog.d("GoogleApiClient: Failed - "+connectionResult.toString());
		}
	}

}

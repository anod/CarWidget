package com.anod.car.home.incar;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

public class BroadcastService extends Service  implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, ModeBroadcastReceiver.UpdateActivityClientListener {
	private ActivityRecognitionClient mActivityRecognitionClient;

    // Constants used to establish the activity update interval
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int DETECTION_INTERVAL_SECONDS = 10;

    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

	public static boolean sRegistered;
    @Override
	public void onCreate() {
   		register(this);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
   		unregister(this);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void register(Context context) {
		AppLog.d("BroadcastService::register");
		sRegistered =true;

		Handler.onRegister(context);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION);
		filter.addAction(ModeBroadcastReceiver.ACTION_UPDATE_ACTIVITY_CLIENT);
		ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
		receiver.setUpdateClientListener(this);
		context.registerReceiver(receiver, filter);

			
		if (PreferencesStorage.isActivityRecognitionEnabled(context)) {
			attachActivityRecognitionClient(context);
		}
	}

	private void unregister(Context context) {
		AppLog.d("BroadcastService::unregister");
		sRegistered =false;
		ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
		receiver.setUpdateClientListener(null);
		context.unregisterReceiver(receiver);


		detachActivityRecognitionClient();
	}


	private void attachActivityRecognitionClient(Context context) {
		if (mActivityRecognitionClient == null) {
			// Connect to the ActivityRecognitionService
			mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
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
		Intent intent = new Intent(this, ActivityRecognitionService.class);
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

	@Override
	public void onUpdate(boolean enable) {
		AppLog.d("ActivityRecognitionClientUpdate: " + enable);
		if (enable) {
			attachActivityRecognitionClient(this);
		} else {
			detachActivityRecognitionClient();
		}
	}
}

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
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

public class BroadcastService extends Service  implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
	private ActivityRecognitionClient mActivityRecognitionClient;

	public static boolean sRegistred;
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
		Log.d("HomeCarWidget", "Register");
		sRegistred=true;

		attachActivityRecognitionClient();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION);
		context.registerReceiver(ModeBroadcastReceiver.getInstance(), filter);

	}

	private void unregister(Context context) {
		Log.d("HomeCarWidget", "unregister");
		sRegistred=false;
		context.unregisterReceiver(ModeBroadcastReceiver.getInstance());


		detachActivityRecognitionClient();
	}


	private void attachActivityRecognitionClient() {
		// Connect to the ActivityRecognitionService
		if (mActivityRecognitionClient == null) {
			mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
			mActivityRecognitionClient.connect();
		}
	}

	private void detachActivityRecognitionClient() {
		if (mActivityRecognitionClient != null) {
			mActivityRecognitionClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Toast.makeText(this, "Activity Recognition Client Connected", Toast.LENGTH_SHORT).show();
		// 3 sec
		mActivityRecognitionClient.requestActivityUpdates(10000, getActivityRecognitionPendingIntent());
	}

	private PendingIntent getActivityRecognitionPendingIntent() {
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Activity Recognition Client Disconnected",Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(this, "Activity Recognition Client Connection Failed: "+connectionResult.toString(),Toast.LENGTH_SHORT).show();
	}
}

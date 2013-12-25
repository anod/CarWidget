package com.anod.car.home.incar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;

public class BroadcastService extends Service {

	private static boolean sRegistered;

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


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		register(this);
		return super.onStartCommand(intent, flags, startId);
	}

	private void register(Context context) {
		AppLog.d("BroadcastService::register");
		if (!sRegistered) {
			sRegistered =true;

			ModeDetector.onRegister(context);

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_HEADSET_PLUG);
	//		filter.addAction(Intent.ACTION_POWER_CONNECTED);
	//		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
	//		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
	//		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
	//		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
			context.registerReceiver(receiver, filter);

			if (PreferencesStorage.isActivityRecognitionEnabled(this)) {
				startService(ActivityRecognitionClientService.makeStartIntent(this));
			}

		}
	}

	private void unregister(Context context) {
		AppLog.d("BroadcastService::unregister");
		sRegistered =false;
		ModeBroadcastReceiver receiver = ModeBroadcastReceiver.getInstance();
		context.unregisterReceiver(receiver);
		stopService(ActivityRecognitionClientService.makeStartIntent(this));
	}



}

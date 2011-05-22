package com.anod.car.home.incar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {

	public static boolean sRegistred = false;
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
		sRegistred=true;

    	IntentFilter filter = new IntentFilter();
    	filter.addAction(Intent.ACTION_HEADSET_PLUG);
    	filter.addAction(Intent.ACTION_POWER_CONNECTED);
    	filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    	filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
    	filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    	filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(ModeBroadcastReceiver.getInstance(), filter);
		Log.d("HomeCarWidget", "Register");
	}

	private void unregister(Context context) {
		sRegistred=false;
        context.unregisterReceiver(ModeBroadcastReceiver.getInstance());
        Log.d("HomeCarWidget", "unregister");
	}	
}

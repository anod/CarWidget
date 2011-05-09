package com.anod.car.home.incar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ModeBroadcastReceiver extends BroadcastReceiver {
	private static ModeBroadcastReceiver sInstance;
	
	public static ModeBroadcastReceiver getInstance() {
	    if(sInstance == null)
	    {
	    	sInstance = new ModeBroadcastReceiver();
	    }
	    return sInstance;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("CarHomeWidget"," Action: "+intent.getAction());
		Handler.onBroadcastReceive(context,intent);
	}
	
		
}

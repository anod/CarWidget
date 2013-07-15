package com.anod.car.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReenableCarModeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d("CarHomeWidget", "onReceive car mode reenable: "+intent);
		// TODO
	}

}

package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 1/15/14
 */
public class BluetoothDevice extends Detection {


	@Override
	public boolean isActive() {
		return true; //mPrefs.isBluetoothRequired();
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_bluetooth_connected;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.pref_blutooth_device_title;
	}

	@Override
	public int getSummaryRes() {
		return R.string.pref_blutooth_device_summary;
	}
}
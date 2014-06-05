package com.anod.car.home.prefs.action;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 6/5/14
 */
public class Bluetooth  extends Action {
	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_bluetooth;
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
package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 1/15/14
 */
public class Headset extends Detection{

	@Override
	public boolean isActive() {
		return true;//mPrefs.isHeadsetRequired();
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_headphones;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.pref_headset_connected_title;
	}

	@Override
	public int getSummaryRes() {
		return R.string.pref_headset_connected_summary;
	}
}

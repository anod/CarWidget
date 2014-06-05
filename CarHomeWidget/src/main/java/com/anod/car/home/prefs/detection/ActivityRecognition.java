package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 1/15/14
 */
public class ActivityRecognition extends Detection {

	@Override
	public boolean isActive() {
		return mPrefs.isActivityRequired();
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_bulb;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.activity_recognition;
	}

	@Override
	public int getSummaryRes() {
		return R.string.gms_success;
	}
}

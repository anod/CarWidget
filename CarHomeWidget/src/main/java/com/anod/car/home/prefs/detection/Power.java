package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

/**
 * @author alex
 * @date 1/15/14
 */
public class Power extends Detection {

	@Override
	public boolean isActive() {
		return mPrefs.isPowerRequired();
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_usb;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.pref_power_connected_title;
	}

	@Override
	public int getSummaryRes() {
		return R.string.pref_power_connected_summary;
	}

	@Override
	public void onClick() {
		mPrefs.setPowerRequired(!mPrefs.isPowerRequired());
		PreferencesStorage.saveInCar(mContext, mPrefs);
	}
}

package com.anod.car.home.prefs.detection;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 1/15/14
 */
public class CarDock extends Detection {


	@Override
	public boolean isActive() {
		return mPrefs.isCarDockRequired();
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_wheel;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.cardock;
	}

	@Override
	public int getSummaryRes() {
		return 0;
	}
}

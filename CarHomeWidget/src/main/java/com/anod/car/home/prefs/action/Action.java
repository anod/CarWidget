package com.anod.car.home.prefs.action;

import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 6/5/14
 */
public abstract class Action {

	protected InCar mPrefs;

	public void setPrefs(InCar prefs) {
		mPrefs = prefs;
	}

	public abstract boolean isActive();
	public abstract int getIconRes();
	public abstract int getShortTitleRes();
	public abstract int getSummaryRes();
}

package com.anod.car.home.prefs.detection;

import android.content.Context;

import com.anod.car.home.prefs.preferences.InCar;

/**
 * @author alex
 * @date 1/15/14
 */
public abstract class Detection implements InCarItem {

	protected Context mContext;
	protected InCar mPrefs;

	public void setPrefs(InCar prefs) {
		mPrefs = prefs;
	}
	public void setContext(Context context) {
		mContext = context;
	}

	public abstract boolean isActive();
	public abstract int getIconRes();
	public abstract int getShortTitleRes();
	public abstract int getSummaryRes();

}

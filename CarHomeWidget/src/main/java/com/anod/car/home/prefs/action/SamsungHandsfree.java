package com.anod.car.home.prefs.action;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 6/5/14
 */
public class SamsungHandsfree extends Action {
	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_wheel;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.samsung_driving_title;
	}

	@Override
	public int getSummaryRes() {
		return R.string.samsung_driving_summary;
	}

	@Override
	public void onClick() {

	}
}
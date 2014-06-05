package com.anod.car.home.prefs.action;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 6/5/14
 */
public class ScreenTimeout extends Action {
	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_brightness_medium;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.pref_screen_timeout;
	}

	@Override
	public int getSummaryRes() {
		return R.string.pref_screen_timeout_summary;
	}
}

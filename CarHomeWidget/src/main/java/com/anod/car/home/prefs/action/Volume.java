package com.anod.car.home.prefs.action;

import com.anod.car.home.R;

/**
 * @author alex
 * @date 6/5/14
 */
public class Volume extends Action {
	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public int getIconRes() {
		return R.drawable.ic_action_volume_up;
	}

	@Override
	public int getShortTitleRes() {
		return R.string.pref_change_media_volume;
	}

	@Override
	public int getSummaryRes() {
		return R.string.pref_change_media_volume_summary;
	}

	@Override
	public void onClick() {

	}
}

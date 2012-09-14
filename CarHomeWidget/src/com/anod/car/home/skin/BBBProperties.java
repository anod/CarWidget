package com.anod.car.home.skin;

import com.anod.car.home.R;


public class BBBProperties extends BaseProperties {

	@Override
	public int getLayout() {
		return R.layout.blackbearblanc;
	}

	@Override
	public IconProcessor getIconProcessor() {
		return new BBBIconProcessor();
	}

	@Override
	public int getSetShortcutRes() {
		return R.drawable.ic_add_shortcut_holo;
	}

	@Override
	public int getSetShortcutText() {
		return R.string.set_shortcut_short;
	}
}

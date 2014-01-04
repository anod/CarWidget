package com.anod.car.home.skin;

import com.anod.car.home.R;

public class GlossyProperties extends BaseProperties {

	@Override
	public int getLayout() {
		return R.layout.glass;
	}

	@Override
	public int getSettingsButtonRes() {
		return R.drawable.ic_settings;
	}

	@Override
	public int getRowLayout() {
		return R.layout.glass_row;
	}
}

package com.anod.car.home.skin;

import com.anod.car.home.R;

public class CarHomeProperties extends BaseProperties {


	@Override
	public int getLayout(int number) {
		if (number == 4) {
			return R.layout.sk_carhome_4;
		}
		if (number == 8) {
			return R.layout.sk_carhome_8;
		}
		return R.layout.sk_carhome_6;
	}

	@Override
	public int getSettingsButtonRes() {
		return R.drawable.ic_settings;
	}

	@Override
	public int getRowLayout() {
		return R.layout.sk_carhome_row;
	}
}

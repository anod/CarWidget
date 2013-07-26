package com.anod.car.home.skin;

import com.anod.car.home.R;

public class CarHomeProperties extends BaseProperties {

	protected CarHomeProperties(boolean keyguard) {
		super(keyguard);
	}

	@Override
	public int getLayout() {
		return R.layout.carhome;
	}
}

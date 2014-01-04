package com.anod.car.home.skin;

import com.anod.car.home.R;

public class MetroProperties extends BaseProperties {

	@Override
	public int getInCarButtonExitRes() {
		return R.drawable.ic_incar_exit_win7;
	}

	@Override
	public int getInCarButtonEnterRes() {
		return R.drawable.ic_incar_enter_win7;
	}

	@Override
	public int getLayout() {
		return R.layout.windows7;
	}

	@Override
	public int getIconPaddingRes() { return 0; }

	@Override
	public int getSettingsButtonRes() {
		return R.drawable.ic_settings_win7;
	}

	@Override
	public int getRowLayout() {
		return R.layout.windows7_row;
	}
}

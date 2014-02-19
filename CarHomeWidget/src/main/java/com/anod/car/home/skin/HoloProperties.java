package com.anod.car.home.skin;

import com.anod.car.home.R;

public class HoloProperties extends BaseProperties {
	@Override
	public int getInCarButtonExitRes() {
		return R.drawable.ic_incar_exit_holo;
	}

	@Override
	public int getInCarButtonEnterRes() {
		return R.drawable.ic_incar_enter_holo;
	}

	@Override
	public int getSetShortcutRes() {
		return R.drawable.ic_add_shortcut_holo;
	}

	@Override
	public int getLayout(int number) {
		if (number == 4) {
			return R.layout.sk_holo_4;
		}
		if (number == 8) {
			return R.layout.sk_holo_8;
		}
		return R.layout.sk_holo_6;
	}
	@Override
	public int getSettingsButtonRes() {
		return R.drawable.ic_holo_settings;
	}

	@Override
	public int getRowLayout() {
		return R.layout.sk_holo_row;
	}
}

package com.anod.car.home.skin;

import com.anod.car.home.R;

public class HoloProperties extends BaseProperties {

	protected HoloProperties(boolean keyguard) {
		super(keyguard);
	}

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
	public int getLayout() {
		return (mIsKeyguard) ? R.layout.holo_keyguard : R.layout.holo;
	}

}

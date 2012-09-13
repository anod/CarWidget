package com.anod.car.home.skin;

import com.anod.car.home.R;

public abstract class BaseProperties implements SkinProperties {

	@Override
	public int getInCarButtonExitRes() {
		return R.drawable.ic_incar_exit;
	}

	@Override
	public int getInCarButtonEnterRes() {
		return R.drawable.ic_incar_enter;
	}
	
	@Override
	public int getSetShortcutRes() {
		return R.drawable.ic_add_shortcut;
	}

	@Override
	public IconProcessor getIconProcessor() {
		return null;
	}

	
	
}
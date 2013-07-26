package com.anod.car.home.skin;

import com.anod.car.home.R;


public class BBBProperties extends BaseProperties {

	protected BBBProperties(boolean keyguard) {
		super(keyguard);
	}

	@Override
	public int getInCarButtonExitRes() {
		return R.drawable.ic_incar_exit_bbb;
	}

	@Override
	public int getInCarButtonEnterRes() {
		return R.drawable.ic_incar_enter_bbb;
	}
	
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

package com.anod.car.home.skin;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.skin.icon.BackgroundProcessor;
import com.anod.car.home.skin.icon.IconProcessor;

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

	@Override
	public int getSetShortcutText() {
		return R.string.set_shortcut;
	}

	@Override
	public int getIconPaddingRes() { return R.dimen.icon_padding_bottom; }

    @Override
    public BackgroundProcessor getBackgroundProcessor() {
        return null;
    }

}

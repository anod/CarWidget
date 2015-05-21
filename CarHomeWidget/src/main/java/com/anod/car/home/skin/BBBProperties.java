package com.anod.car.home.skin;

import com.anod.car.home.R;
import com.anod.car.home.skin.icon.BBBIconProcessor;
import com.anod.car.home.skin.icon.IconProcessor;


public class BBBProperties extends BaseProperties {

    @Override
    public int getInCarButtonExitRes() {
        return R.drawable.ic_incar_exit_bbb;
    }

    @Override
    public int getInCarButtonEnterRes() {
        return R.drawable.ic_incar_enter_bbb;
    }

    @Override
    public int getLayout(int number) {
        if (number == 4) {
            return R.layout.sk_blackbearblanc_4;
        }
        if (number == 8) {
            return R.layout.sk_blackbearblanc_8;
        }
        return R.layout.sk_blackbearblanc_6;
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

    @Override
    public int getIconPaddingRes() {
        return 0;
    }

    @Override
    public int getSettingsButtonRes() {
        return R.drawable.ic_settings_bbb;
    }

    @Override
    public int getRowLayout() {
        return R.layout.sk_blackbearblanc_row;
    }
}

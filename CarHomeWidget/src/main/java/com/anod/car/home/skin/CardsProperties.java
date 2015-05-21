package com.anod.car.home.skin;

import com.anod.car.home.R;
import com.anod.car.home.skin.icon.BackgroundProcessor;
import com.anod.car.home.skin.icon.CardsBackgroundProcessor;

public class CardsProperties extends BaseProperties {

    @Override
    public int getInCarButtonExitRes() {
        return R.drawable.ic_incar_exit_gray;
    }

    @Override
    public int getInCarButtonEnterRes() {
        return R.drawable.ic_incar_enter_gray;
    }

    @Override
    public int getSetShortcutRes() {
        return R.drawable.ic_add_shortcut_holo;
    }

    @Override
    public int getLayout(int number) {
        if (number == 4) {
            return R.layout.sk_material_4;
        }
        if (number == 8) {
            return R.layout.sk_material_8;
        }
        return R.layout.sk_material_6;
    }

    @Override
    public int getSettingsButtonRes() {
        return R.drawable.ic_settings_grey600_36dp;
    }

    @Override
    public int getRowLayout() {
        return R.layout.sk_material_row;
    }

    @Override
    public int getIconPaddingRes() {
        return 0;
    }

    @Override
    public boolean hasWidgetButton1() {
        return false;
    }

    @Override
    public BackgroundProcessor getBackgroundProcessor() {
        return new CardsBackgroundProcessor();
    }
}

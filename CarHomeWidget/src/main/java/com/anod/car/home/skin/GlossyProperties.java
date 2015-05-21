package com.anod.car.home.skin;

import com.anod.car.home.R;

public class GlossyProperties extends BaseProperties {

    @Override
    public int getLayout(int number) {
        if (number == 4) {
            return R.layout.sk_glass_4;
        }
        if (number == 8) {
            return R.layout.sk_glass_8;
        }
        return R.layout.sk_glass_6;
    }

    @Override
    public int getSettingsButtonRes() {
        return R.drawable.ic_settings;
    }

    @Override
    public int getRowLayout() {
        return R.layout.sk_glass_row;
    }
}

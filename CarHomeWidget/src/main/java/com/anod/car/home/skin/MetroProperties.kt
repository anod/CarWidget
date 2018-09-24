package com.anod.car.home.skin

import com.anod.car.home.R
import com.anod.car.home.skin.icon.BackgroundProcessor
import com.anod.car.home.skin.icon.MetroBackgroundProcessor

class MetroProperties : BaseProperties() {

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_win7

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_win7

    override val iconPaddingRes: Int
        get() = 0

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings_win7

    override val rowLayout: Int
        get() = R.layout.sk_windows7_row

    override val backgroundProcessor: BackgroundProcessor?
        get() = MetroBackgroundProcessor()

    override fun getLayout(number: Int): Int {
        if (number == 4) {
            return R.layout.sk_windows7_4
        }
        return if (number == 8) {
            R.layout.sk_windows7_8
        } else R.layout.sk_windows7_6
    }
}

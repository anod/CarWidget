package com.anod.car.home.skin

import com.anod.car.home.R

class HoloProperties : BaseProperties() {

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_holo

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_holo

    override val setShortcutRes: Int
        get() = R.drawable.ic_add_shortcut_holo

    override val settingsButtonRes: Int
        get() = R.drawable.ic_holo_settings

    override val rowLayout: Int
        get() = R.layout.sk_holo_row

    override fun getLayout(number: Int): Int {
        if (number == 4) {
            return R.layout.sk_holo_4
        }
        return if (number == 8) {
            R.layout.sk_holo_8
        } else R.layout.sk_holo_6
    }
}

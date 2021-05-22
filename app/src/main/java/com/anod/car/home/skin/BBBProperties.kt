package com.anod.car.home.skin

import com.anod.car.home.R
import com.anod.car.home.skin.icon.BBBIconProcessor
import info.anodsplace.carwidget.utils.IconProcessor

class BBBProperties : BaseProperties() {

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_bbb

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_bbb

    override val iconProcessor: IconProcessor
        get() = BBBIconProcessor()

    override val setShortcutRes: Int
        get() = R.drawable.ic_add_shortcut_holo

    override val setShortcutText: Int
        get() = R.string.set_shortcut_short

    override val iconPaddingRes: Int
        get() = 0

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings_bbb

    override val rowLayout: Int
        get() = R.layout.sk_blackbearblanc_row

    override fun getLayout(number: Int): Int {
        return when (number) {
            4 -> R.layout.sk_blackbearblanc_4
            8 -> R.layout.sk_blackbearblanc_8
            10 -> R.layout.sk_blackbearblanc_10
            else -> R.layout.sk_blackbearblanc_6
        }
    }
}

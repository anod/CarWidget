package com.anod.car.home.skin

import com.anod.car.home.R
import com.anod.car.home.skin.icon.CardsBackgroundProcessor
import info.anodsplace.carwidget.content.IconBackgroundProcessor

class CardsProperties : BaseProperties() {

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_gray

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_gray

    override val setShortcutRes: Int
        get() = R.drawable.ic_add_shortcut_holo

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings_grey600_36dp

    override val rowLayout: Int
        get() = R.layout.sk_material_row

    override val iconPaddingRes: Int
        get() = 0

    override val backgroundProcessor: IconBackgroundProcessor
        get() = CardsBackgroundProcessor()

    override fun getLayout(number: Int): Int {
        return when (number) {
            4 -> R.layout.sk_material_4
            8 -> R.layout.sk_material_8
            10 -> R.layout.sk_material_10
            else -> R.layout.sk_material_6
        }
    }

    override fun hasWidgetButton1(): Boolean {
        return false
    }
}
package com.anod.car.home.skin

import com.anod.car.home.R
import com.anod.car.home.skin.icon.BackgroundProcessor
import com.anod.car.home.skin.icon.CardsBackgroundProcessor

class YouProperties : BaseProperties() {

    override val inCarButtonExitRes: Int
        get() = R.drawable.you_wheel_exit

    override val inCarButtonEnterRes: Int
        get() = R.drawable.you_wheel

    override val setShortcutRes: Int
        get() = R.drawable.you_add_24

    override val settingsButtonRes: Int
        get() = R.drawable.you_edit_24

    override val rowLayout: Int
        get() = R.layout.sk_you_row

    override val iconPaddingRes: Int
        get() = 0

    override val fontColorRes: Int
        get() = 0

    override fun getLayout(number: Int): Int {
        return when (number) {
            4 -> R.layout.sk_you_4
            8 -> R.layout.sk_you_8
            10 -> R.layout.sk_you_10
            else -> R.layout.sk_you_6
        }
    }

    override fun hasWidgetButton1(): Boolean {
        return true
    }
}

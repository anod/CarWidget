package com.anod.car.home.skin

import com.anod.car.home.R

class GlossyProperties : BaseProperties() {

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings

    override val rowLayout: Int
        get() = R.layout.sk_glass_row

    override fun getLayout(number: Int): Int {
        return when (number) {
            4 -> R.layout.sk_glass_4
            8 -> R.layout.sk_glass_8
            10 -> R.layout.sk_glass_10
            else -> R.layout.sk_glass_6
        }
    }
}

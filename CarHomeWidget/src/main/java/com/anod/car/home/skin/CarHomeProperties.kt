package com.anod.car.home.skin

import com.anod.car.home.R

class CarHomeProperties : BaseProperties() {

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings

    override val rowLayout: Int
        get() = R.layout.sk_carhome_row


    override fun getLayout(number: Int): Int {
        if (number == 4) {
            return R.layout.sk_carhome_4
        }
        return if (number == 8) {
            R.layout.sk_carhome_8
        } else R.layout.sk_carhome_6
    }
}

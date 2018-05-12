package com.anod.car.home.skin

import com.anod.car.home.prefs.preferences.Main

object PropertiesFactory {

    fun create(skinName: String): SkinProperties {
        return when (skinName) {
            Main.SKIN_WINDOWS7 -> MetroProperties()
            Main.SKIN_HOLO -> HoloProperties()
            Main.SKIN_GLOSSY -> GlossyProperties()
            Main.SKIN_BBB -> BBBProperties()
            Main.SKIN_CARDS -> CardsProperties()
            else -> CarHomeProperties()
        }
    }

}

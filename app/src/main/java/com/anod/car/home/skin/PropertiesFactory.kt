package com.anod.car.home.skin

import info.anodsplace.carwidget.content.preferences.WidgetInterface

object PropertiesFactory {

    fun create(skinName: String): SkinProperties {
        return when (skinName) {
            WidgetInterface.SKIN_WINDOWS7 -> MetroProperties()
            WidgetInterface.SKIN_HOLO -> HoloProperties()
            WidgetInterface.SKIN_GLOSSY -> GlossyProperties()
            WidgetInterface.SKIN_BBB -> BBBProperties()
            WidgetInterface.SKIN_CARDS -> CardsProperties()
            WidgetInterface.SKIN_YOU -> YouProperties()
            else -> CarHomeProperties()
        }
    }

}

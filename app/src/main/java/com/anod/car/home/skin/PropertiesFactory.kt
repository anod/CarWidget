package com.anod.car.home.skin

import info.anodsplace.carwidget.content.preferences.WidgetInterface

object PropertiesFactory {

    fun create(skinName: String): SkinProperties {
        return when (skinName) {
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_WINDOWS7 -> MetroProperties()
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_HOLO -> HoloProperties()
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_GLOSSY -> GlossyProperties()
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_BBB -> BBBProperties()
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_CARDS -> CardsProperties()
            info.anodsplace.carwidget.content.preferences.WidgetInterface.SKIN_YOU -> YouProperties()
            else -> CarHomeProperties()
        }
    }

}

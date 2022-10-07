package info.anodsplace.carwidget.skin

import android.content.Context
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.preferences.WidgetInterface

object SkinPropertiesFactory {
    fun create(skinName: String, context: Context): SkinProperties {
        return when (skinName) {
            WidgetInterface.SKIN_WINDOWS7 -> MetroProperties()
            WidgetInterface.SKIN_HOLO -> HoloProperties()
            WidgetInterface.SKIN_GLOSSY -> GlossyProperties()
            WidgetInterface.SKIN_BBB -> BBBProperties()
            WidgetInterface.SKIN_CARDS -> CardsProperties()
            WidgetInterface.SKIN_YOU -> YouProperties(context)
            else -> CarHomeProperties()
        }
    }
}
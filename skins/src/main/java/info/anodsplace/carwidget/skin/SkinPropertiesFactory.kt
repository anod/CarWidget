package info.anodsplace.carwidget.skin

import android.content.Context
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.preferences.WidgetInterface

class SkinPropertiesFactory(private val context: Context) : SkinProperties.Factory {
    override fun create(skinName: String): SkinProperties {
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
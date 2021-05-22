package info.anodsplace.carwidget.preferences.model

import info.anodsplace.carwidget.utils.BitmapTransform

/**
 * @author algavris
 * @date 09/04/2016.
 */
interface WidgetInterface {
    var iconsTheme: String
    var isSettingsTransparent: Boolean
    var isIncarTransparent: Boolean
    var skin: String
    var tileColor: Int? // nullable because old backups can have null value
    var isIconsMono: Boolean
    var iconsColor: Int?
    val iconsScale: String
    var fontColor: Int
    var fontSize: Int
    var backgroundColor: Int
    var iconsRotate: BitmapTransform.RotateDirection
    var isTitlesHide: Boolean
    var widgetButton1: Int
    var widgetButton2: Int

    fun setIconsScaleString(iconsScale: String)

    companion object {
        const val idUnknown: Long = -1

        const val SKIN_GLOSSY = "glossy"
        const val SKIN_CARHOME = "carhome"
        const val SKIN_WINDOWS7 = "windows7"
        const val SKIN_HOLO = "holo"
        const val SKIN_BBB = "blackbearblanc"
        const val SKIN_CARDS = "cards"
        const val SKIN_YOU = "you"
        const val FONT_SIZE_UNDEFINED = -1
        const val WIDGET_BUTTON_INCAR = 1
        const val WIDGET_BUTTON_SETTINGS = 2
        const val WIDGET_BUTTON_HIDDEN = 3
    }
}

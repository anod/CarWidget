package info.anodsplace.carwidget.content.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

enum class BitmapRotateDirection {
    NONE, RIGHT, LEFT
}

/**
 * @author algavris
 * @date 09/04/2016.
 */
interface WidgetInterface {
    val changes: Flow<Pair<String, Any?>>

    var isFirstTime: Boolean
    var iconsTheme: String
    var isSettingsTransparent: Boolean
    var isIncarTransparent: Boolean
    var skin: String
    var tileColor: Int
    var paletteBackground: Boolean
    var isIconsMono: Boolean
    var iconsColor: Int?
    var iconsScale: String
    var fontColor: Int?
    var fontSize: Int
    var backgroundColor: Int
    var iconsRotate: BitmapRotateDirection
    var isTitlesHide: Boolean
    var widgetButton1: Int
    var widgetButton2: Int
    var shortcutsNumber: Int
    val adaptiveIconStyle: String

    fun queueChange(key: String, value: Any?)
    fun applyChange(key: String, value: Any?)
    fun applyPending()
    fun <T : Any?> observe(key: String): Flow<T>

    data class NoOp(
        override var isFirstTime: Boolean = true,
        override var iconsTheme: String = "",
        override var isSettingsTransparent: Boolean = false,
        override var isIncarTransparent: Boolean = false,
        override var skin: String = SKIN_YOU,
        override var tileColor: Int = 0,
        override var isIconsMono: Boolean = false,
        override var iconsColor: Int? = null,
        override var iconsScale: String = ICONS_DEF_SCALE,
        override var fontColor: Int? = null,
        override var fontSize: Int = FONT_SIZE_UNDEFINED,
        override var backgroundColor: Int = 0,
        override var iconsRotate: BitmapRotateDirection = BitmapRotateDirection.NONE,
        override var isTitlesHide: Boolean = false,
        override var widgetButton1: Int = WIDGET_BUTTON_INCAR,
        override var widgetButton2: Int = WIDGET_BUTTON_SETTINGS,
        override var shortcutsNumber: Int = WidgetStorage.LAUNCH_COMPONENT_NUMBER_DEFAULT,
        override var paletteBackground: Boolean = false,
        override var adaptiveIconStyle: String = ""
    ) : WidgetInterface {

        constructor(other: WidgetInterface) : this(
            isFirstTime = other.isFirstTime,
            iconsTheme = other.iconsTheme,
            isSettingsTransparent = other.isSettingsTransparent,
            isIncarTransparent = other.isIncarTransparent,
            skin = other.skin,
            tileColor = other.tileColor,
            isIconsMono = other.isIconsMono,
            iconsColor = other.iconsColor,
            iconsScale = other.iconsScale,
            fontColor = other.fontColor,
            fontSize = other.fontSize,
            backgroundColor = other.backgroundColor,
            iconsRotate = other.iconsRotate,
            isTitlesHide = other.isTitlesHide,
            widgetButton1 = other.widgetButton1,
            widgetButton2 = other.widgetButton2,
            shortcutsNumber = other.shortcutsNumber,
            paletteBackground = other.paletteBackground,
            adaptiveIconStyle = other.adaptiveIconStyle,
        )

        override val changes = emptyFlow<Pair<String, Any?>>()
        override fun queueChange(key: String, value: Any?) {}
        override fun applyChange(key: String, value: Any?) {}
        override fun applyPending() {}
        override fun <T : Any?> observe(key: String): Flow<T> = flowOf()
    }

    companion object {
        const val idUnknown: Long = -1

        const val BUTTON_ID_1 = 1
        const val BUTTON_ID_2 = 2

        const val WIDGET_BUTTON_INCAR = 1
        const val WIDGET_BUTTON_SETTINGS = 2
        const val WIDGET_BUTTON_HIDDEN = 3

        private const val ICONS_DEF_SCALE = "5"

        const val SKIN_GLOSSY = "glossy"
        const val SKIN_CARHOME = "carhome"
        const val SKIN_WINDOWS7 = "windows7"
        const val SKIN_HOLO = "holo"
        const val SKIN_BBB = "blackbearblanc"
        const val SKIN_CARDS = "cards"
        const val SKIN_YOU = "you"
        const val FONT_SIZE_UNDEFINED = -1

        val skins = listOf(
            SKIN_YOU, SKIN_CARDS, SKIN_HOLO, SKIN_GLOSSY, SKIN_CARHOME, SKIN_WINDOWS7, SKIN_BBB
        )

        fun convertIconsScale(scaleString: String): Float {
            return convertIconsScale(scaleString.toInt())
        }

        fun convertIconsScale(scale: Int): Float {
            return 1.0f + 0.1f * scale
        }
    }
}
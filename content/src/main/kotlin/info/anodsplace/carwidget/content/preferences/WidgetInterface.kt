package info.anodsplace.carwidget.content.preferences

enum class BitmapRotateDirection {
    NONE, RIGHT, LEFT
}

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
    var fontColor: Int?
    var fontSize: Int
    var backgroundColor: Int
    var iconsRotate: BitmapRotateDirection
    var isTitlesHide: Boolean
    var widgetButton1: Int
    var widgetButton2: Int

    fun setIconsScaleString(iconsScale: String)
    fun queueChange(key: String, value: Any?)
    fun applyChange(key: String, value: Any?)
    fun apply()

    class NoOp(
        override var iconsTheme: String = "",
        override var isSettingsTransparent: Boolean = false,
        override var isIncarTransparent: Boolean = false,
        override var skin: String = "",
        override var tileColor: Int? = null,
        override var isIconsMono: Boolean = false,
        override var iconsColor: Int? = null,
        override val iconsScale: String = "",
        override var fontColor: Int? = null,
        override var fontSize: Int = 0,
        override var backgroundColor: Int = 0,
        override var iconsRotate: BitmapRotateDirection = BitmapRotateDirection.NONE,
        override var isTitlesHide: Boolean = false,
        override var widgetButton1: Int = 0,
        override var widgetButton2: Int = 0
    ) : WidgetInterface {
        override fun setIconsScaleString(iconsScale: String) { }
        override fun queueChange(key: String, value: Any?) {}
        override fun applyChange(key: String, value: Any?) {}
        override fun apply() {}
    }

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

        val skins = listOf(
            SKIN_YOU, SKIN_CARDS, SKIN_HOLO, SKIN_GLOSSY, SKIN_CARHOME, SKIN_WINDOWS7, SKIN_BBB
        )

    }
}

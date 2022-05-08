package info.anodsplace.carwidget.content.preferences

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Path
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.collection.SimpleArrayMap
import androidx.core.graphics.PathParser
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import java.io.IOException
import java.lang.Integer.min

/**
 * @author algavris
 * @date 09/04/2016.
 */
class WidgetSettings(context: Context, appWidgetIdScope: AppWidgetIdScope, private val defaults: DefaultsProvider, appScope: AppCoroutineScope)
    : ChangeableSharedPreferences(WidgetStorage.getSharedPreferences(context, +appWidgetIdScope), appScope), WidgetInterface {

    interface DefaultsProvider {
        val tileColor: Int
        val backgroundColor: Int
    }

    override var isFirstTime: Boolean
        get() = prefs.getBoolean(FIRST_TIME, true)
        set(value) = applyChange(FIRST_TIME, value)

    override var iconsTheme: String
        get() = prefs.getString(ICONS_THEME, "")!!
        set(iconsTheme) = applyChange(ICONS_THEME, iconsTheme)

    override var isSettingsTransparent: Boolean
        get() = prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false)
        set(settingsTransparent) = applyChange(TRANSPARENT_BTN_SETTINGS, settingsTransparent)

    override var isIncarTransparent: Boolean
        get() = prefs.getBoolean(TRANSPARENT_BTN_INCAR, false)
        set(incarTransparent) = applyChange(TRANSPARENT_BTN_INCAR, incarTransparent)

    override var skin: String
        get() {
            val value = prefs.getString(SKIN, WidgetInterface.SKIN_YOU)!!
            if (value.isEmpty()) {
                return WidgetInterface.SKIN_YOU
            }
            return value
        }
        set(skin) = applyChange(SKIN, skin)

    override var tileColor: Int
        get() {
            return prefs.getInt(BUTTON_COLOR, defaults.tileColor)
        }
        set(tileColor) = applyChange(BUTTON_COLOR, tileColor)

    override var isIconsMono: Boolean
        get() = prefs.getBoolean(ICONS_MONO, false)
        set(iconsMono) = applyChange(ICONS_MONO, iconsMono)

    override var iconsColor: Int?
        get() = getColor(ICONS_COLOR, prefs)
        set(iconsColor) = applyChange(ICONS_COLOR, iconsColor)

    override var iconsScale: String
        get() = prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE)!!
        set(value) { applyChange(ICONS_SCALE, value) }

    override var fontColor: Int?
        get() = if (prefs.contains(FONT_COLOR))  {
                val color = prefs.getInt(FONT_COLOR, Color.WHITE)
                if (color == Color.WHITE) null else color
            } else null
        set(fontColor) {
            applyChange(FONT_COLOR, fontColor)
        }

    override var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, WidgetInterface.FONT_SIZE_UNDEFINED)
        set(fontSize) = applyChange(FONT_SIZE, fontSize)

    override var backgroundColor: Int
        get() = prefs.getInt(BG_COLOR, defaults.backgroundColor)
        set(backgroundColor) = applyChange(BG_COLOR, backgroundColor)

    override var iconsRotate: BitmapRotateDirection
        get() = BitmapRotateDirection
                .valueOf(prefs.getString(ICONS_ROTATE, BitmapRotateDirection.NONE.name)!!)
        set(iconsRotate) = applyChange(ICONS_ROTATE, iconsRotate.name)

    override var isTitlesHide: Boolean
        get() = prefs.getBoolean(TITLES_HIDE, false)
        set(titlesHide) = applyChange(TITLES_HIDE, titlesHide)

    override var widgetButton1: Int
        get() = prefs.getInt(WIDGET_BUTTON_1, WidgetInterface.WIDGET_BUTTON_INCAR)
        set(widgetButton1) = applyChange(WIDGET_BUTTON_1, widgetButton1)

    override var widgetButton2: Int
        get() = prefs.getInt(WIDGET_BUTTON_2, WidgetInterface.WIDGET_BUTTON_SETTINGS)
        set(widgetButton2) = applyChange(WIDGET_BUTTON_2, widgetButton2)

    var adaptiveIconStyle: String
        get() = prefs.getString(ADAPTIVE_ICON_STYLE, "")!!
        set(style) = applyChange(ADAPTIVE_ICON_STYLE, style)

    override var paletteBackground: Boolean
        get() = prefs.getBoolean(PALETTE_BG, false)
        set(paletteBackground) = applyChange(PALETTE_BG, paletteBackground)

   override val adaptiveIconPath: Path
        get() {
            val pathData = adaptiveIconStyle
            return when {
                pathData.isNotBlank() -> {
                    PathParser.createPathFromPathData(pathData)
                }
                else -> Path()
            }
        }

    override var shortcutsNumber: Int
        get() {
            val num = prefs.getInt(WidgetStorage.CMP_NUMBER, WidgetStorage.LAUNCH_COMPONENT_NUMBER_DEFAULT)
            return if (num == 0) WidgetStorage.LAUNCH_COMPONENT_NUMBER_DEFAULT else min(num, WidgetStorage.LAUNCH_COMPONENT_NUMBER_MAX)
        }
        set(value) = applyChange(WidgetStorage.CMP_NUMBER, min(value, WidgetStorage.LAUNCH_COMPONENT_NUMBER_MAX))

    @Throws(IOException::class)
    fun writeJson(writer: JsonWriter) {
        writer.beginObject()
        writer.name(FIRST_TIME).value(isFirstTime)

        writer.name(SKIN).value(skin)

        writer.name(BG_COLOR).value(backgroundColor.toLong())
        writer.name(BUTTON_COLOR).value(tileColor)

        writer.name(ICONS_MONO).value(isIconsMono)
        writer.name(ICONS_COLOR).value(iconsColor)
        writer.name(ICONS_SCALE).value(iconsScale)
        writer.name(ICONS_THEME).value(iconsTheme)
        writer.name(ICONS_ROTATE).value(iconsRotate.name)

        writer.name(FONT_SIZE).value(fontSize.toLong())
        if (fontColor != null) {
            writer.name(FONT_COLOR).value(fontColor!!.toLong())
        }

        writer.name(TRANSPARENT_BTN_SETTINGS).value(isSettingsTransparent)
        writer.name(TRANSPARENT_BTN_INCAR).value(isIncarTransparent)

        writer.name(TITLES_HIDE).value(isTitlesHide)

        writer.name(WIDGET_BUTTON_1).value(widgetButton1.toLong())
        writer.name(WIDGET_BUTTON_2).value(widgetButton2.toLong())

        writer.name(ADAPTIVE_ICON_STYLE).value(adaptiveIconStyle)
        writer.name(PALETTE_BG).value(paletteBackground)
        writer.endObject()
    }

    @Throws(IOException::class)
    fun readJson(reader: JsonReader): Int {

        val types = SimpleArrayMap<String, JsonToken>()
        types.put(FIRST_TIME, JsonToken.BOOLEAN)
        types.put(SKIN, JsonToken.STRING)

        types.put(BG_COLOR, JsonToken.NUMBER)
        types.put(BUTTON_COLOR, JsonToken.NUMBER)

        types.put(ICONS_MONO, JsonToken.BOOLEAN)
        types.put(ICONS_COLOR, JsonToken.NUMBER)
        types.put(ICONS_SCALE, JsonToken.STRING)
        types.put(ICONS_THEME, JsonToken.STRING)
        types.put(ICONS_ROTATE, JsonToken.STRING)

        types.put(FONT_SIZE, JsonToken.NUMBER)
        types.put(FONT_COLOR, JsonToken.NUMBER)

        types.put(TRANSPARENT_BTN_SETTINGS, JsonToken.BOOLEAN)
        types.put(TRANSPARENT_BTN_INCAR, JsonToken.BOOLEAN)

        types.put(TITLES_HIDE, JsonToken.BOOLEAN)

        types.put(WIDGET_BUTTON_1, JsonToken.NUMBER)
        types.put(WIDGET_BUTTON_2, JsonToken.NUMBER)

        types.put(ADAPTIVE_ICON_STYLE, JsonToken.STRING)
        types.put("palette-background", JsonToken.BOOLEAN)
        reader.beginObject()
        val found = JsonReaderHelper.readValues(reader, types, this) { _, _ -> false }
        reader.endObject()
        return found
    }

    companion object {
        private const val SKIN = "skin"
        const val BG_COLOR = "bg-color"
        const val BUTTON_COLOR = "button-color"
        const val PALETTE_BG = "palette-background"
        private const val ICONS_MONO = "icons-mono"
        const val ICONS_COLOR = "icons-color"
        private const val ICONS_SCALE = "icons-scale"
        const val FONT_SIZE = "font-size"
        const val FONT_COLOR = "font-color"
        private const val FIRST_TIME = "first-time"
        const val TRANSPARENT_BTN_SETTINGS = "transparent-btn-settings"
        const val TRANSPARENT_BTN_INCAR = "transparent-btn-incar"
        private const val ICONS_THEME = "icons-theme"
        const val ICONS_ROTATE = "icons-rotate"
        const val TITLES_HIDE = "titles-hide"
        private const val WIDGET_BUTTON_1 = "widget-button-1"
        private const val WIDGET_BUTTON_2 = "widget-button-2"
        private const val ICONS_DEF_VALUE = "5"
        const val ADAPTIVE_ICON_STYLE = "adaptive-icon-style"

        private fun getColor(key: String, prefs: SharedPreferences): Int? {
            return if (!prefs.contains(key)) {
                null
            } else prefs.getInt(key, Color.WHITE)
        }
    }
}
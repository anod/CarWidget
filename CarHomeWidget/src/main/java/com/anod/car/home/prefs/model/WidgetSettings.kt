package com.anod.car.home.prefs.model

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Path
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.collection.SimpleArrayMap
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter

import com.anod.car.home.R
import com.anod.car.home.utils.BitmapTransform
import info.anodsplace.framework.graphics.PathParser

import java.io.IOException

/**
 * @author algavris
 * @date 09/04/2016.
 */
class WidgetSettings(prefs: SharedPreferences, private val mResources: Resources) : ChangeableSharedPreferences(prefs), WidgetInterface {

    var isFirstTime: Boolean
        get() = prefs.getBoolean(FIRST_TIME, true)
        set(value) = putChange(FIRST_TIME, value)

    override var iconsTheme: String
        get() = prefs.getString(ICONS_THEME, "")!!
        set(iconsTheme) = putChange(ICONS_THEME, iconsTheme)

    override var isSettingsTransparent: Boolean
        get() = prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false)
        set(settingsTransparent) = putChange(TRANSPARENT_BTN_SETTINGS, settingsTransparent)

    override var isIncarTransparent: Boolean
        get() = prefs.getBoolean(TRANSPARENT_BTN_INCAR, false)
        set(incarTransparent) = putChange(TRANSPARENT_BTN_INCAR, incarTransparent)

    override var skin: String
        get() = prefs.getString(SKIN, WidgetInterface.SKIN_CARDS)!!
        set(skin) = putChange(SKIN, skin)

    override var tileColor: Int?
        get() {
            val defTileColor = ResourcesCompat.getColor(mResources, R.color.w7_tale_default_background, null)
            return prefs.getInt(BUTTON_COLOR, defTileColor)
        }
        set(tileColor) = putChange(BUTTON_COLOR, tileColor)

    override var isIconsMono: Boolean
        get() = prefs.getBoolean(ICONS_MONO, false)
        set(iconsMono) = putChange(ICONS_MONO, iconsMono)

    override var iconsColor: Int?
        get() = getColor(ICONS_COLOR, prefs)
        set(iconsColor) = putChange(ICONS_COLOR, iconsColor)

    override val iconsScale: String
        get() = prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE)!!

    override var fontColor: Int
        get() = prefs.getInt(FONT_COLOR, ResourcesCompat.getColor(mResources, R.color.default_font_color, null))
        set(fontColor) = putChange(FONT_COLOR, fontColor)

    override var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, WidgetInterface.FONT_SIZE_UNDEFINED)
        set(fontSize) = putChange(FONT_SIZE, fontSize)

    override var backgroundColor: Int
        get() = prefs.getInt(BG_COLOR, ResourcesCompat.getColor(mResources, R.color.default_background, null))
        set(backgroundColor) = putChange(BG_COLOR, backgroundColor)

    override var iconsRotate: BitmapTransform.RotateDirection
        get() = BitmapTransform.RotateDirection
                .valueOf(prefs.getString(ICONS_ROTATE, BitmapTransform.RotateDirection.NONE.name)!!)
        set(iconsRotate) = putChange(ICONS_ROTATE, iconsRotate.name)

    override var isTitlesHide: Boolean
        get() = prefs.getBoolean(TITLES_HIDE, false)
        set(titlesHide) = putChange(TITLES_HIDE, titlesHide)

    override var widgetButton1: Int
        get() = prefs.getInt(WIDGET_BUTTON_1, WidgetInterface.WIDGET_BUTTON_INCAR)
        set(widgetButton1) = putChange(WIDGET_BUTTON_1, widgetButton1)

    override var widgetButton2: Int
        get() = prefs.getInt(WIDGET_BUTTON_2, WidgetInterface.WIDGET_BUTTON_SETTINGS)
        set(widgetButton2) = putChange(WIDGET_BUTTON_2, widgetButton2)

    var adaptiveIconStyle: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) prefs.getString(ADAPTIVE_ICON_STYLE, "")!! else ""
        set(style) = putChange(ADAPTIVE_ICON_STYLE, style)

    val adaptiveIconPath: Path
        get() {
            val pathData = adaptiveIconStyle
            return when {
                pathData.isNotBlank() -> {
                    PathParser.createPathFromPathData(pathData)
                }
                else -> Path()
            }
        }

    override fun setIconsScaleString(iconsScale: String) {
        putChange(ICONS_SCALE, iconsScale)
    }

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
        writer.name(FONT_COLOR).value(fontColor.toLong())

        writer.name(TRANSPARENT_BTN_SETTINGS).value(isSettingsTransparent)
        writer.name(TRANSPARENT_BTN_INCAR).value(isIncarTransparent)

        writer.name(TITLES_HIDE).value(isTitlesHide)

        writer.name(WIDGET_BUTTON_1).value(widgetButton1.toLong())
        writer.name(WIDGET_BUTTON_2).value(widgetButton2.toLong())

        writer.name(ADAPTIVE_ICON_STYLE).value(adaptiveIconStyle)
        writer.endObject()
    }

    @Throws(IOException::class)
    fun readJson(reader: JsonReader) {
        reader.beginObject()

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

        JsonReaderHelper.readValues(reader, types, this)

        reader.endObject()
    }

    companion object {
        private const val SKIN = "skin"
        const val BG_COLOR = "bg-color"
        const val BUTTON_COLOR = "button-color"
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

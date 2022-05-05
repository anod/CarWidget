package info.anodsplace.carwidget.content

import android.graphics.Bitmap
import info.anodsplace.carwidget.content.preferences.WidgetSettings

/**
 * @author alex
 * @date 2014-12-06
 */
interface IconBackgroundProcessor {
    fun getColor(prefs: WidgetSettings, icon: Bitmap?): Int
}
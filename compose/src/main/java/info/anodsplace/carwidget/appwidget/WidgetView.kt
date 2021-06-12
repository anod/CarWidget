package info.anodsplace.carwidget.appwidget

import android.widget.RemoteViews
import info.anodsplace.carwidget.content.IconTheme

interface WidgetView {
    val appWidgetId: Int
    fun init(overrideSkin: String? = null)
    fun refreshIconTransform()
    fun create(): RemoteViews
    fun loadThemeIcons(themePackage: String): IconTheme?
}
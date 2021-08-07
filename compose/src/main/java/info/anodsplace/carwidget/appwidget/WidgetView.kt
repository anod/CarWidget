package info.anodsplace.carwidget.appwidget

import android.widget.RemoteViews
import info.anodsplace.carwidget.content.IconTheme

interface WidgetView {
    var overrideSkin: String?
    val appWidgetId: Int
    fun init()
    fun refreshIconTransform()
    fun create(): RemoteViews
    fun loadThemeIcons(themePackage: String): IconTheme?
}
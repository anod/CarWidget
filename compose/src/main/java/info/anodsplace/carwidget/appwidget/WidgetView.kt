package info.anodsplace.carwidget.appwidget

import android.widget.RemoteViews
import info.anodsplace.carwidget.content.IconTheme

interface DummyWidgetView {
    val overrideSkin: String?
    fun refreshIconTransform()
    suspend fun create(): RemoteViews
    fun loadThemeIcons(themePackage: String): IconTheme?
}

interface WidgetView : DummyWidgetView
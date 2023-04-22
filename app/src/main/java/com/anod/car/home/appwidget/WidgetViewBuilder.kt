package com.anod.car.home.appwidget

import android.content.Context
import android.widget.RemoteViews
import com.anod.car.home.incar.ModeService
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.SkinWidgetView
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.graphics.BitmapTransform
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

class WidgetViewBuilder(
    private val context: Context,
    private val iconLoader: ShortcutIconLoader,
    private val appWidgetId: Int,
    private val widgetSettings: WidgetInterface,
    private val inCarSettings: InCarInterface,
    private val koin: Koin,
    private val shortcutsModel: Shortcuts,
    private val bitmapMemoryCache: BitmapLruCache?,
    private val pendingIntentFactory: PendingIntentFactory,
    private val widgetButtonAlternativeHidden: Boolean,
    private val overrideSkin: String?,
    private val overrideCount: Int?
) : WidgetView {

    private val bitmapTransform = BitmapTransform(context)

    suspend fun firstTimeInit() {
        if (widgetSettings.isFirstTime) { // TODO: race condition between multiple views in preview
            widgetSettings.isFirstTime = false
            shortcutsModel.createDefaultShortcuts()
            widgetSettings.applyPending()
        }
    }

    override suspend fun create(): RemoteViews = withContext(Dispatchers.Default) {
        shortcutsModel.init()

        val skinName = overrideSkin ?: widgetSettings.skin
        val skinProperties: SkinProperties = koin.get { parametersOf(skinName) }
        val shortcuts = if (overrideCount == null) shortcutsModel.shortcuts else shortcutsModel.shortcuts.filterKeys { it < overrideCount }
        val skinWidgetView = SkinWidgetView(
            appWidgetId = appWidgetId,
            inCarMode = ModeService.sInCarMode,
            widgetButtonAlternativeHidden = widgetButtonAlternativeHidden,
            pendingIntentFactory = pendingIntentFactory,
            inCarSettings = inCarSettings,
            iconLoader = iconLoader,
            bitmapMemoryCache = bitmapMemoryCache,
            skinProperties = skinProperties,
            shortcuts = shortcuts,
            widgetSettings = widgetSettings,
            bitmapTransform = bitmapTransform,
            context = context
        )

        return@withContext skinWidgetView.create()
    }
}
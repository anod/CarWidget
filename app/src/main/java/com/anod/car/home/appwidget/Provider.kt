package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import com.anod.car.home.LargeProvider
import com.anod.car.home.incar.ModeService
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.random.Random

open class Provider : AppWidgetProvider(), KoinComponent {

    init {
        AppLog.tag = "CarWidget"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        requestUpdate(context, appWidgetIds, appWidgetManager) {
            pendingResult.finish()
        }
    }

    /**
     * Will be executed when the widget is removed from the homescreen
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        AppLog.i("appWidgetIds: ${appWidgetIds.joinToString(",")}", tag = "onDeleted")
        val scope: AppCoroutineScope = get()
        scope.launch {
            // Drop the settings if the widget is deleted
            WidgetStorage.dropWidgetSettings(get(), context, appWidgetIds)
        }
    }

    override fun onDisabled(context: Context) {
        AppLog.i( "", tag = "onDisabled")
        getKoin().get<BroadcastServiceManager>().stopService()

        if (ModeService.sInCarMode) {
            val modeIntent = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_OFF)
            context.stopService(modeIntent)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                           appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        AppLog.i("appWidgetId: $appWidgetId", tag ="onAppWidgetOptionsChanged")
    }

    companion object : KoinComponent {

        fun requestUpdate(
            context: Context,
            appWidgetIds: IntArray,
            appWidgetManager: AppWidgetManager,
            onComplete: (() -> Unit)? = null
        ) {
            AppLog.i("appWidgetIds: ${appWidgetIds.joinToString(",")}", tag = "requestUpdate")
            val ids = if (appWidgetIds.isEmpty()) {
                appWidgetManager.getAppWidgetIds(getComponentName(context))
            } else {
                appWidgetIds
            }
            if (ids.isEmpty()) {
                AppLog.w("appWidgetIds is empty, skipping update", tag = "requestUpdate")
                onComplete?.invoke()
                return
            }
            val scope: AppCoroutineScope = get()
            scope.launch(Dispatchers.Default) {
                try {
                    performUpdate(context.applicationContext, appWidgetManager, get(), ids)
                } catch (e: Exception) {
                    AppLog.e(e)
                } finally {
                    onComplete?.invoke()
                }
            }
        }

        private suspend fun performUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            shortcutResources: ShortcutResources,
            appWidgetIds: IntArray
        ) {
            for (appWidgetId in appWidgetIds) {
                AppWidgetIdScope(appWidgetId, instance = Random.nextInt(), existingScope = null).use {
                    val viewBuilder = WidgetViewBuilder(
                        context = context,
                        iconLoader = get(),
                        appWidgetId = appWidgetId,
                        bitmapMemoryCache = null,
                        pendingIntentFactory = ShortcutPendingIntent(context, shortcutResources),
                        widgetButtonAlternativeHidden = false,
                        overrideSkin = null,
                        overrideCount = null,
                        widgetSettings = it.scope.get(),
                        inCarSettings = get(),
                        shortcutsModel = it.scope.get(),
                        koin = getKoin(),
                    )
                    viewBuilder.firstTimeInit()
                    val view = viewBuilder.create()
                    AppLog.i("Performing update for widget #$appWidgetId")
                    appWidgetManager.updateAppWidget(appWidgetId, view)
                }
            }
        }

        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, LargeProvider::class.java)
        }
    }
}
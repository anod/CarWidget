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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

open class Provider : AppWidgetProvider(), KoinComponent {

    init {
        AppLog.tag = "CarWidget"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val finished = AtomicBoolean(false)
        requestUpdate(context, appWidgetIds, appWidgetManager) {
            // Completion may be signalled from the coroutine or a synchronous failure path; keep it
            // idempotent and swallow a stray finish() so the callback can never crash the process.
            if (finished.compareAndSet(false, true)) {
                try {
                    pendingResult.finish()
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
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

        // Serializes all widget rebuilds process-wide: a single shared mutex, locked per widget
        // in performUpdate. WidgetViewBuilder.firstTimeInit performs a check-then-write on shared
        // storage, so overlapping rebuilds -- even of different widgets -- must not run
        // concurrently. The lock is held across the suspending create() call, so it also guards
        // work that runs on other dispatchers, which limitedParallelism(1) alone would not.
        private val updateMutex = Mutex()

        fun requestUpdate(
            context: Context,
            appWidgetIds: IntArray,
            appWidgetManager: AppWidgetManager,
            onComplete: (() -> Unit)? = null
        ) {
            AppLog.i("appWidgetIds: ${appWidgetIds.joinToString(",")}", tag = "requestUpdate")
            // Guard the whole hand-off: callers such as onUpdate() have already taken a goAsync()
            // PendingResult, so a synchronous throw here (e.g. Koin not ready) must never escape
            // without invoking onComplete, otherwise the broadcast is left unfinished.
            try {
                val appContext = context.applicationContext
                val ids = if (appWidgetIds.isEmpty()) {
                    appWidgetManager.getAppWidgetIds(getComponentName(appContext))
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
                        performUpdate(appContext, appWidgetManager, get(), ids)
                    } catch (e: Exception) {
                        AppLog.e(e)
                    } finally {
                        onComplete?.invoke()
                    }
                }
            } catch (e: Exception) {
                AppLog.e(e)
                onComplete?.invoke()
            }
        }

        private suspend fun performUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            shortcutResources: ShortcutResources,
            appWidgetIds: IntArray
        ) {
            for (appWidgetId in appWidgetIds) {
                try {
                    updateMutex.withLock {
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
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
        }

        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, LargeProvider::class.java)
        }
    }
}
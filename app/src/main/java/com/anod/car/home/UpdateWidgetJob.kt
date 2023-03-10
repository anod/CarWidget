package com.anod.car.home

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.appwidget.WidgetViewBuilder
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * WorkManager can't be used since it causes updates loop
 * https://issuetracker.google.com/issues/115575872
 * https://commonsware.com/blog/2018/11/24/workmanager-app-widgets-side-effects.html
 */
class UpdateWidgetJob : JobIntentService(), KoinComponent {

    companion object {
        private const val inputWidgetIds = "appWidgetIds"
        private const val jobId = 1000

        fun enqueue(context: Context, appWidgetIds: IntArray) {
            val intent = Intent()
            intent.putExtra(inputWidgetIds, appWidgetIds)
            enqueueWork(context, UpdateWidgetJob::class.java, jobId, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val appWidgetIds = intent.extras!!.getIntArray(inputWidgetIds) ?: intArrayOf()
        val appWidgetManager = get<AppWidgetManager>()
        val shortcutResources = get<ShortcutResources>()
        performUpdate(applicationContext, appWidgetManager, shortcutResources, appWidgetIds)
    }

    private fun performUpdate(context: Context, appWidgetManager: AppWidgetManager, shortcutResources: ShortcutResources, appWidgetIds: IntArray) = runBlocking {
        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (appWidgetId in appWidgetIds) {
            AppWidgetIdScope(appWidgetId).use {
                val view = WidgetViewBuilder(
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
                ).create()
                AppLog.i("Performing update for widget #$appWidgetId")
                appWidgetManager.updateAppWidget(appWidgetId, view)
            }
        }
    }
}
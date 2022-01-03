package com.anod.car.home

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.anod.car.home.app.App
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.incar.BroadcastService
import info.anodsplace.carwidget.content.Version
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

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
        performUpdate(applicationContext, appWidgetIds)
    }

    private fun performUpdate(context: Context, appWidgetIds: IntArray) {
        val appWidgetManager = App.provide(context).appWidgetManager
        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        val shortcutResources = App.provide(context).shortcutResources
        for (appWidgetId in appWidgetIds) {
            val views = WidgetViewBuilder(context, get(), appWidgetId, ShortcutPendingIntent(context, shortcutResources)).apply {
                init()
            }.create()
            AppLog.i("Performing update for widget #$appWidgetId")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

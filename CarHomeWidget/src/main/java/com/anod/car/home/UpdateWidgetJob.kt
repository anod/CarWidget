package com.anod.car.home

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Handler
import androidx.work.Worker

import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.prefs.model.PrefsMigrate
import com.anod.car.home.utils.Version
import info.anodsplace.framework.AppLog
import android.os.Looper
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage


class UpdateWidgetJob : Worker() {
    companion object {
        const val inputWidgetIds = "appWidgetIds"
    }

    var mainHandler = Handler(Looper.getMainLooper())

    override fun doWork(): Result {
        val appWidgetIds = inputData.getIntArray(inputWidgetIds) ?: intArrayOf()
        performUpdate(applicationContext, appWidgetIds)
        return Worker.Result.SUCCESS
    }

    private fun performUpdate(context: Context, appWidgetIds: IntArray) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val version = Version(context)

        registerBroadcastService(context, version.isProOrTrial)
        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (i in 0 until appWidgetIds.size) {
            val appWidgetId = appWidgetIds[i]
            if (!WidgetStorage.hasSettingsFile(applicationContext, appWidgetId)) {
                AppLog.w("No settings for $appWidgetId, skipping update...")
                continue
            }
            val builder = WidgetViewBuilder(context, appWidgetId, ShortcutPendingIntent(context))
            val views = builder.init().build()
            mainHandler.post {
                AppLog.d("Performing update for $appWidgetId")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

    }

    private fun registerBroadcastService(context: Context, isProOrTrial: Boolean) {
        val inCarEnabled = if (isProOrTrial)
            InCarStorage.load(context).isInCarEnabled
        else
            false
        if (inCarEnabled) {
            BroadcastService.startService(context)
        } else {
            BroadcastService.stopService(context)
        }
    }

}

package com.anod.car.home

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.anod.car.home.app.App
import com.anod.car.home.appwidget.ShortcutPendingIntent
import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.Version
import info.anodsplace.framework.AppLog

/**
 * WorkManager can't be used since it causes updates loop
 * https://issuetracker.google.com/issues/115575872
 * https://commonsware.com/blog/2018/11/24/workmanager-app-widgets-side-effects.html
 */
class UpdateWidgetJob : JobIntentService() {

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
        val version = Version(context)

        registerBroadcastService(context, version.isProOrTrial)
        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (element in appWidgetIds) {
            val builder = WidgetViewBuilder(context, element, ShortcutPendingIntent(context))
            val views = builder.init().build()
            AppLog.i("Performing update for widget #$element")
            appWidgetManager.updateAppWidget(element, views)
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

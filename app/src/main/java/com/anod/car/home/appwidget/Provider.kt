package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.anod.car.home.LargeProvider
import com.anod.car.home.UpdateWidgetJob
import com.anod.car.home.incar.ModeService
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class Provider : AppWidgetProvider(), KoinComponent {

    init {
        AppLog.tag = "CarWidget"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        requestUpdate(context, appWidgetIds, appWidgetManager)
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
        val updateIntent = Intent(context, UpdateWidgetJob::class.java)
        context.stopService(updateIntent)

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

    companion object {

        fun requestUpdate(context: Context, appWidgetIds: IntArray, appWidgetManager: AppWidgetManager) {
            AppLog.i("appWidgetIds: ${appWidgetIds.joinToString(",")}", tag = "requestUpdate")
            if (appWidgetIds.isEmpty()) {
                val thisAppWidget = getComponentName(context)
                val allAppWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
                enqueue(allAppWidgetIds, context)
            } else {
                enqueue(appWidgetIds, context)
            }
        }

        private fun enqueue(appWidgetIds: IntArray, context: Context) {
            if (appWidgetIds.isEmpty()) {
                AppLog.w("appWidgetIds is empty, skipp[ing update", tag = "enqueue")
                return
            }
            AppLog.i("appWidgetIds: ${appWidgetIds.joinToString(",")}", tag = "enqueue")
            UpdateWidgetJob.enqueue(context, appWidgetIds)
        }

        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, LargeProvider::class.java)
        }
    }
}
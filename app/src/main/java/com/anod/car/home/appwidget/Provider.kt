package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.anod.car.home.LargeProvider
import com.anod.car.home.UpdateWidgetJob
import com.anod.car.home.app.App
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeService
import com.anod.car.home.prefs.model.WidgetStorage
import info.anodsplace.framework.AppLog


open class Provider : AppWidgetProvider() {

    init {
        AppLog.tag = "CarWidget"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        requestUpdate(context, appWidgetIds)
    }

    /**
     * Will be executed when the widget is removed from the homescreen
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        AppLog.e("CarWidget", "onDeleted: ${appWidgetIds.joinToString(",")}")
        // Drop the settings if the widget is deleted
        WidgetStorage.dropWidgetSettings(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        AppLog.e("CarWidget", "onDisabled")
        val updateIntent = Intent(context, UpdateWidgetJob::class.java)
        context.stopService(updateIntent)

        BroadcastService.stopService(context)

        if (ModeService.sInCarMode) {
            val modeIntent = ModeService
                    .createStartIntent(context, ModeService.MODE_SWITCH_OFF)
            context.stopService(modeIntent)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                           appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        AppLog.e("CarWidget", "onAppWidgetOptionsChanged: ${appWidgetId}")
        val maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1)

        val myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1)
        val isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD

        if (isKeyguard && maxHeight != -1) {
            AppLog.d("CarWidgetOptions", "isKeyguard: $isKeyguard, maxHeight: $maxHeight")
            requestUpdate(context, intArrayOf(appWidgetId))
        }
    }

    companion object {

        fun requestUpdate(context: Context, appWidgetIds: IntArray) {
            AppLog.e("requestUpdate: ${appWidgetIds.joinToString(",")}")
            if (appWidgetIds.isEmpty()) {
                val appWidgetManager = App.provide(context).appWidgetManager
                val thisAppWidget = getComponentName(context)
                val allAppWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
                enqueue(allAppWidgetIds, context)
            } else {
                enqueue(appWidgetIds, context)
            }
        }

        private fun enqueue(appWidgetIds: IntArray, context: Context) {
            if (appWidgetIds.isEmpty()) {
                AppLog.w("appWidgetIds is empty, skipp[ing update")
                return
            }
            AppLog.e("enqueue: ${appWidgetIds.joinToString(",")}")
            UpdateWidgetJob.enqueue(context, appWidgetIds)
        }

        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, LargeProvider::class.java)
        }
    }
}

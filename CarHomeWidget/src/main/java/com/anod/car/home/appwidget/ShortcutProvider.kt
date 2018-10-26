package com.anod.car.home.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

import com.anod.car.home.R
import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.SwitchInCarActivity
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.Version

/**
 * @author algavris
 * @date 28/07/2016.
 */
class ShortcutProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val views = RemoteViews(context.packageName, R.layout.widget_incar_mode)

        val activity = Intent(context, SwitchInCarActivity::class.java)
        activity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY

        val switchIntent = PendingIntent.getActivity(context, 0, activity, PendingIntent.FLAG_UPDATE_CURRENT)
        //        pendingIntent.setData(Uri.parse("com.anod.car.home.pro://mode/switch"));
        views.setOnClickPendingIntent(R.id.button, switchIntent)

        val version = Version(context)
        registerBroadcastService(context, version.isProOrTrial)

        appWidgetManager.updateAppWidget(appWidgetIds, views)
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

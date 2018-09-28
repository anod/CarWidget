package com.anod.car.home.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

import com.anod.car.home.R
import com.anod.car.home.incar.SwitchInCarActivity

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

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

}

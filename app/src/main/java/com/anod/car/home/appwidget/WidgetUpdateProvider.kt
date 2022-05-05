package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import info.anodsplace.carwidget.appwidget.WidgetUpdate

class WidgetUpdateProvider(private val context: Context, private val appWidgetManager: AppWidgetManager): WidgetUpdate {
    override fun request(appWidgetIds: IntArray) {
        Provider.requestUpdate(context, appWidgetIds, appWidgetManager)
    }
}
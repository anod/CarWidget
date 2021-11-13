package com.anod.car.home.appwidget

import android.content.Context
import info.anodsplace.carwidget.appwidget.WidgetUpdate

class WidgetUpdateProvider(private val context: Context): WidgetUpdate {
    override fun request(appWidgetIds: IntArray) {
        Provider.requestUpdate(context, appWidgetIds)
    }
}
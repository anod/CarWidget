package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

import com.anod.car.home.LargeProvider
import com.anod.car.home.app.App
import info.anodsplace.carwidget.appwidget.WidgetIds

/**
 * @author alex
 * @date 5/24/13
 */
class WidgetHelper(private val context: Context) : WidgetIds {
    private val appWidgetManager: AppWidgetManager
        get() = AppWidgetManager.getInstance(context)

    override fun getLargeWidgetIds(): IntArray {
        val provider = ComponentName(context, LargeProvider::class.java)
        return appWidgetManager.getAppWidgetIds(provider)
    }

    override fun getShortcutWidgetIds(): IntArray {
        val provider = ComponentName(context, ShortcutProvider::class.java)
        return appWidgetManager.getAppWidgetIds(provider)
    }

    override fun getAllWidgetIds(): IntArray {
        return getLargeWidgetIds() + getShortcutWidgetIds()
    }
}

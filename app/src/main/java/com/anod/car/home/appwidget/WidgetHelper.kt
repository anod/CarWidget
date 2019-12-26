package com.anod.car.home.appwidget

import android.content.ComponentName
import android.content.Context

import com.anod.car.home.LargeProvider
import com.anod.car.home.app.App

/**
 * @author alex
 * @date 5/24/13
 */
object WidgetHelper {

    fun getLargeWidgetIds(context: Context): IntArray {
        val provider = ComponentName(context, LargeProvider::class.java)
        return App.provide(context).appWidgetManager.getAppWidgetIds(provider)
    }

    fun getShortcutWidgetIds(context: Context): IntArray {
        val provider = ComponentName(context, ShortcutProvider::class.java)
        return App.provide(context).appWidgetManager.getAppWidgetIds(provider)
    }

    fun getAllWidgetIds(context: Context): IntArray {
        val array1 = getLargeWidgetIds(context)
        val array2 = getShortcutWidgetIds(context)
        return array1 + array2
    }

}

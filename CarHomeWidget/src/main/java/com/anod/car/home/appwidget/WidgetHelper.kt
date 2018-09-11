package com.anod.car.home.appwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

import com.anod.car.home.LargeProvider

/**
 * @author alex
 * @date 5/24/13
 */
object WidgetHelper {

    /**
     * Build [ComponentName] describing this specific
     * [android.appwidget.AppWidgetProvider]
     */
    fun getLargeWidgetIds(context: Context): IntArray {
        val applicationContext = context.applicationContext
        val provider = ComponentName(applicationContext, LargeProvider::class.java)
        return AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(provider)
    }

    fun getShortcutWidgetIds(context: Context): IntArray {
        val applicationContext = context.applicationContext
        val wm = AppWidgetManager.getInstance(applicationContext)
        val provider = ComponentName(applicationContext, ShortcutProvider::class.java)

        return wm.getAppWidgetIds(provider)
    }

    fun getAllWidgetIds(context: Context): IntArray {
        val array1 = getLargeWidgetIds(context)
        val array2 = getShortcutWidgetIds(context)

        val total = IntArray(array1.size + array2.size)
        System.arraycopy(array1, 0, total, 0, array1.size)
        System.arraycopy(array2, 0, total, array1.size, array2.size)

        return total
    }

}

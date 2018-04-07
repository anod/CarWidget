package com.anod.car.home.ui

import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel

import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import android.util.SparseArray

/**
 * @author alex
 * @date 5/27/13
 */
class WidgetsListLoader(context: Context) : AsyncTaskLoader<WidgetsListLoader.Result>(context) {

    class Result {
        var large: SparseArray<SparseArray<Shortcut>> = SparseArray()
        var shortcuts: IntArray = intArrayOf()
    }

    override fun loadInBackground(): Result {

        val appWidgetIds = WidgetHelper.getLargeWidgetIds(context)
        val result = Result()

        for (i in appWidgetIds.indices) {
            val model = WidgetShortcutsModel(context, appWidgetIds[i])
            model.init()

            result.large.put(appWidgetIds[i], model.shortcuts)
        }

        result.shortcuts = WidgetHelper.getShortcutWidgetIds(context)
        return result
    }

}

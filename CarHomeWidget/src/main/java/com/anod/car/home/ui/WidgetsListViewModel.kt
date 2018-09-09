package com.anod.car.home.ui

import android.app.Application
import androidx.lifecycle.MutableLiveData
import android.util.SparseArray
import androidx.core.util.isEmpty
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.utils.AppViewModel
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask

class WidgetList {
    var large: SparseArray<SparseArray<Shortcut>> = SparseArray()
    var shortcuts: IntArray = intArrayOf()

    val isEmpty: Boolean
        get() = large.isEmpty() && shortcuts.isEmpty()
}

class WidgetsListViewModel(application: Application) : AppViewModel(application) {
    val list = MutableLiveData<WidgetList>()

    fun loadList() {
        BackgroundTask(object : BackgroundTask.Worker<Void?, WidgetList>(app, null) {
            override fun run(param: Void?, context: ApplicationContext): WidgetList {
                val appWidgetIds = WidgetHelper.getLargeWidgetIds(context.actual)
                val result = WidgetList()

                for (i in appWidgetIds.indices) {
                    val model = WidgetShortcutsModel(context.actual, appWidgetIds[i])
                    model.init()

                    result.large.put(appWidgetIds[i], model.shortcuts)
                }

                result.shortcuts = WidgetHelper.getShortcutWidgetIds(context.actual)
                return result
            }

            override fun finished(result: WidgetList) {
                list.value = result
            }
        }).execute()
    }
}
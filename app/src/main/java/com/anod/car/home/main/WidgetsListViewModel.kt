package com.anod.car.home.main

import android.app.Application
import android.content.Context
import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anod.car.home.appwidget.WidgetHelper
import info.anodsplace.carwidget.db.Shortcut
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.utils.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetList {
    var large: SparseArray<SparseArray<Shortcut?>> = SparseArray()
    var shortcuts: IntArray = intArrayOf()

    val isEmpty: Boolean
        get() = large.isEmpty() && shortcuts.isEmpty()
}

class WidgetsListViewModel(application: Application) : AppViewModel(application) {
    val list = MutableLiveData<WidgetList>()

    fun loadList() {
        viewModelScope.launch {
            val result = loadWidgetList(app)
            list.value = result
        }
    }

    private suspend fun loadWidgetList(context: Context): WidgetList = withContext(Dispatchers.Default) {
        val appWidgetIds = WidgetHelper.getLargeWidgetIds(context)
        val result = WidgetList()

        for (i in appWidgetIds.indices) {
            val model = WidgetShortcutsModel(context, appWidgetIds[i])
            model.init()

            result.large.put(appWidgetIds[i], model.shortcuts)
        }

        result.shortcuts = WidgetHelper.getShortcutWidgetIds(context)
        return@withContext result
    }
}
package info.anodsplace.carwidget.screens

import android.app.Application
import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.compose.ScreenLoadState
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

class WidgetList {
    var large: SparseArray<SparseArray<Shortcut?>> = SparseArray()
    var shortcuts: IntArray = intArrayOf()

    val isEmpty: Boolean
        get() = large.isEmpty() && shortcuts.isEmpty()
}

interface WidgetItem {

    class Large(
        val appWidgetId: Int,
        val shortcuts: SparseArray<info.anodsplace.carwidget.content.db.Shortcut?>,
        val adaptiveIconStyle: String
    ) : WidgetItem

    class Shortcut : WidgetItem
}

class WidgetsListViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val widgetIds: WidgetIds by inject(WidgetIds::class.java)
    private val context: Context
        get() = getApplication()

    fun loadScreen(): Flow<ScreenLoadState<List<WidgetItem>>> = flow {
        val list = loadWidgetList(getApplication())
        val newItems = mutableListOf<WidgetItem>()
        newItems.addAll(list.shortcuts.map { WidgetItem.Shortcut() })

        val defaultsProvider = DefaultsResourceProvider(context)
        list.large.forEach { key, value ->
            newItems.add(WidgetItem.Large(
                key, value, WidgetStorage.load(getApplication(), defaultsProvider, key).adaptiveIconStyle
            ))
        }

        emit(ScreenLoadState.Ready(newItems))
    }

    private suspend fun loadWidgetList(context: Context): WidgetList = withContext(Dispatchers.Default) {
        val appWidgetIds = widgetIds.getLargeWidgetIds()
        val result = WidgetList()

        val defaultsProvider = DefaultsResourceProvider(context)
        for (i in appWidgetIds.indices) {
            val model = WidgetShortcutsModel(context, defaultsProvider, appWidgetIds[i])
            model.init()

            result.large.put(appWidgetIds[i], model.shortcuts)
        }

        result.shortcuts = widgetIds.getShortcutWidgetIds()
        return@withContext result
    }
}
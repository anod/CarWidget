package info.anodsplace.carwidget.screens.main

import android.app.Application
import android.content.Context
import android.util.SparseArray
import androidx.annotation.StringRes
import androidx.core.util.isEmpty
import androidx.lifecycle.AndroidViewModel
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.compose.ScreenLoadState
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
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
    data class Large(
        val appWidgetId: Int,
        val shortcuts: List<info.anodsplace.carwidget.content.db.Shortcut?>,
        val adaptiveIconStyle: String
    ) : WidgetItem

    data class Shortcut(
        val appWidgetId: Int
    ) : WidgetItem
}

data class WidgetListScreenState(
    val items: List<WidgetItem>,
    val isServiceRequired: Boolean,
    val isServiceRunning: Boolean,
    val eventsState: List<InCarStatus.EventState>,
    @StringRes val statusResId: Int
)

class WidgetsListViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val widgetIds: WidgetIds by inject(WidgetIds::class.java)
    private val inCarStatus: InCarStatus by inject(InCarStatus::class.java)
    private val context: Context
        get() = getApplication()

    fun loadScreen(): Flow<ScreenLoadState<WidgetListScreenState>> = flow {
        val newItems = loadWidgetList(context)
        emit(ScreenLoadState.Ready(
            WidgetListScreenState(
                items = newItems,
                isServiceRequired = inCarStatus.isServiceRequired,
                isServiceRunning = inCarStatus.isServiceRunning,
                statusResId = inCarStatus.resId,
                eventsState = inCarStatus.eventsState().sortedWith(compareBy({ !it.enabled }, { !it.active }))
            )
        ))
    }

    private suspend fun loadWidgetList(context: Context): List<WidgetItem> = withContext(Dispatchers.Default) {
        val newItems = mutableListOf<WidgetItem>()
        val defaultsProvider = DefaultsResourceProvider(context)

        val appWidgetIds = widgetIds.getLargeWidgetIds()
        newItems.addAll(widgetIds.getShortcutWidgetIds().map { WidgetItem.Shortcut(appWidgetId = it) })

        for (appWidgetId in appWidgetIds) {
            val model = WidgetShortcutsModel(context, defaultsProvider, appWidgetId).apply {
                init()
            }
            val shortcuts = model.shortcuts
            newItems.add(
                WidgetItem.Large(
                    appWidgetId, shortcuts.values.toList(), WidgetStorage.load(getApplication(), defaultsProvider, appWidgetId).adaptiveIconStyle
                )
            )
        }

        return@withContext newItems
    }
}
package info.anodsplace.carwidget.screens.main

import android.content.Context
import android.os.PowerManager
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.BroadcastServiceManager
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

interface WidgetItem {
    data class Large(
        val appWidgetId: Int,
        val shortcuts: Map<Int, info.anodsplace.carwidget.content.db.Shortcut?>,
        val adaptiveIconStyle: String,
        val skinName: String
    ) : WidgetItem

    data class Shortcut(
        val appWidgetId: Int
    ) : WidgetItem
}

sealed interface WidgetListLoadState {
    object Loading: WidgetListLoadState
    object Ready: WidgetListLoadState
    data class Error(val message: String, val cause: Exception? = null): WidgetListLoadState
}

data class WidgetListScreenState(
    val loadState: WidgetListLoadState = WidgetListLoadState.Loading,
    val items: List<WidgetItem> = emptyList(),
    val isServiceRequired: Boolean = false,
    val isServiceRunning: Boolean = false,
    val eventsState: List<InCarStatus.EventState> = emptyList(),
    @StringRes val statusResId: Int = 0,
    val ignoringBatteryOptimization: Boolean = false
)

sealed interface WidgetListScreenEvent {
    object LoadWidgetList : WidgetListScreenEvent
}

sealed interface WidgetListScreenAction

class WidgetsListViewModel : BaseFlowViewModel<WidgetListScreenState, WidgetListScreenEvent, WidgetListScreenAction>(), KoinComponent {
    private val widgetIds: WidgetIds by inject()
    private val context: Context by inject()
    private val db: ShortcutsDatabase by inject()
    private val inCarSettings: InCarSettings by inject()
    private val broadcastServiceManager: BroadcastServiceManager by inject()
    private val powerManager: PowerManager? = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    init {
        val inCarStatus: InCarStatus = get()
        viewState = WidgetListScreenState(
            items = emptyList(),
            isServiceRequired = broadcastServiceManager.isServiceRequired,
            isServiceRunning = broadcastServiceManager.isServiceRunning,
            ignoringBatteryOptimization = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true,
            statusResId = inCarStatus.resId,
            eventsState = inCarStatus.eventsState().sortedWith(compareBy({ !it.enabled }, { !it.active }))
        )
        viewModelScope.launch {
            db.observeChanges().collect {
                AppLog.d("LoadWidgetList $it")
                handleEvent(WidgetListScreenEvent.LoadWidgetList)
            }
        }
        viewModelScope.launch {
            inCarSettings
                .changes
                .filter { it.first == InCarSettings.INCAR_MODE_ENABLED }
                .distinctUntilChanged()
                .collect {
                    broadcastServiceManager.registerBroadcastService()
                    updateState(
                        loadState = viewState.loadState,
                        items = viewState.items
                    )
                }
        }
        handleEvent(WidgetListScreenEvent.LoadWidgetList)
    }

    override fun handleEvent(event: WidgetListScreenEvent) {
        when (event) {
            WidgetListScreenEvent.LoadWidgetList -> {
                viewModelScope.launch {
                    try {
                        val newItems = loadWidgetList()
                        updateState(loadState = WidgetListLoadState.Ready, items = newItems)
                    } catch (e: Exception) {
                        updateState(loadState = WidgetListLoadState.Error(e.message ?: "Loading error", e), items = emptyList())
                    }
                }
            }
        }
    }

    private fun updateState(loadState: WidgetListLoadState, items: List<WidgetItem>) {
        val inCarStatus: InCarStatus = get()
        viewState = viewState.copy(
            loadState = loadState,
            items = items,
            isServiceRequired = broadcastServiceManager.isServiceRequired,
            isServiceRunning = broadcastServiceManager.isServiceRunning,
            statusResId = inCarStatus.resId,
            eventsState = inCarStatus.eventsState().sortedWith(compareBy({ !it.enabled }, { !it.active }))
        )
    }

    private suspend fun loadWidgetList(): List<WidgetItem> = withContext(Dispatchers.Default) {
        val newItems = mutableListOf<WidgetItem>()

        val appWidgetIds = widgetIds.getLargeWidgetIds().sorted()
        newItems.addAll(widgetIds.getShortcutWidgetIds().map { WidgetItem.Shortcut(appWidgetId = it) })

        for (appWidgetId in appWidgetIds) {
            AppWidgetIdScope(appWidgetId).use {
                val model = it.scope.get<WidgetShortcutsModel>().apply {
                    init()
                }
                val widgetSettings = it.scope.get<WidgetSettings>()
                val shortcuts = model.shortcuts
                newItems.add(
                    WidgetItem.Large(
                        appWidgetId = appWidgetId,
                        shortcuts = shortcuts,
                        adaptiveIconStyle = widgetSettings.adaptiveIconStyle,
                        skinName = widgetSettings.skin
                    )
                )
            }
        }

        return@withContext newItems
    }

}
package info.anodsplace.carwidget.screens.main

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.compose.ScreenLoadState
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

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

data class WidgetListScreenState(
    val loadState: ScreenLoadState<Unit> = ScreenLoadState.Loading,
    val items: List<WidgetItem> = emptyList(),
    val isServiceRequired: Boolean = false,
    val isServiceRunning: Boolean = false,
    val eventsState: List<InCarStatus.EventState> = emptyList(),
    @StringRes val statusResId: Int = 0
)

sealed interface WidgetListScreenEvent {
    object LoadWidgetList : WidgetListScreenEvent
}

sealed interface WidgetListScreenAction

class WidgetsListViewModel : BaseFlowViewModel<WidgetListScreenState, WidgetListScreenEvent, WidgetListScreenAction>(), KoinComponent {
    private val widgetIds: WidgetIds by inject(WidgetIds::class.java)
    private val inCarStatus: InCarStatus by inject(InCarStatus::class.java)

    init {
        viewState = WidgetListScreenState(
            items = emptyList(),
            isServiceRequired = inCarStatus.isServiceRequired,
            isServiceRunning = inCarStatus.isServiceRunning,
            statusResId = inCarStatus.resId,
            eventsState = inCarStatus.eventsState().sortedWith(compareBy({ !it.enabled }, { !it.active }))
        )
        handleEvent(WidgetListScreenEvent.LoadWidgetList)
    }

    override fun handleEvent(event: WidgetListScreenEvent) {
        when (event) {
            WidgetListScreenEvent.LoadWidgetList -> {
                viewModelScope.launch {
                    try {
                        val newItems = loadWidgetList()
                        updateState(loadState = ScreenLoadState.Ready(Unit), items = newItems)
                    } catch (e: Exception) {
                        updateState(loadState = ScreenLoadState.Error(e.message ?: "Loading error", e), items = emptyList())
                    }
                }
            }
        }
    }

    private fun updateState(loadState: ScreenLoadState<Unit>, items: List<WidgetItem>) {
        viewState = viewState.copy(
            loadState = loadState,
            items = items,
            isServiceRequired = inCarStatus.isServiceRequired,
            isServiceRunning = inCarStatus.isServiceRunning,
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
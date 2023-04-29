package info.anodsplace.carwidget.shortcut

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.WidgetUpdate
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.shortcuts.CreateShortcutResult
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

data class ShortcutPickerViewState(
    val position: Int = -1
)

sealed interface ShortcutPickerViewEvent {
    class Save(val intent: Intent, val isApp: Boolean) : ShortcutPickerViewEvent
}

sealed interface ShortcutPickerViewAction {
    data class CreateResult(val result: CreateShortcutResult) : ShortcutPickerViewAction
}

class ShortcutPickerViewModel(
    position: Int,
    private val appWidgetIdScope: AppWidgetIdScope,
) : BaseFlowViewModel<ShortcutPickerViewState, ShortcutPickerViewEvent, ShortcutPickerViewAction>(), KoinScopeComponent {

    class Factory(
        private val position: Int,
        private val appWidgetIdScope: AppWidgetIdScope,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShortcutPickerViewModel(position, appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val appScope: AppCoroutineScope by inject()
    private val update: WidgetUpdate by inject()
    private val model by inject<WidgetShortcutsModel>()
    val shortcutResources: ShortcutResources by inject()
    val imageLoader: ImageLoader by inject()

    init {
        viewState = ShortcutPickerViewState(
            position = position
        )
    }

    override fun handleEvent(event: ShortcutPickerViewEvent) {
        when(event) {
            is ShortcutPickerViewEvent.Save -> save(event.intent, event.isApp)
        }
    }

    private fun save(intent: Intent, isApp: Boolean) {
        appScope.launch {
            try {
                val result = model.saveIntent(viewState.position, intent, isApp)
                update.request(intArrayOf(+appWidgetIdScope))
                emitAction(ShortcutPickerViewAction.CreateResult(result.second))
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }
    }
}
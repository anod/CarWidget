package info.anodsplace.carwidget.shortcut

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Immutable
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
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

@Immutable
data class ShortcutPickerViewState(
    val position: Int = -1
)

sealed interface ShortcutPickerViewEvent {
    class Save(val intent: Intent, val isApp: Boolean) : ShortcutPickerViewEvent
    class LaunchShortcutError(val exception: Exception) : ShortcutPickerViewEvent
}

sealed interface ShortcutPickerViewAction {
    class ActivityAction(val action: CommonActivityAction.ShowToast) : ShortcutPickerViewAction
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
            is ShortcutPickerViewEvent.LaunchShortcutError -> {
                AppLog.e(event.exception)
                emitAction(ShortcutPickerViewAction.ActivityAction(
                    action = CommonActivityAction.ShowToast(text = "Cannot launch shortcut ${event.exception.message}")
                ))
            }
        }
    }

    private fun save(intent: Intent, isApp: Boolean) {
        appScope.launch {
            try {
                val result = model.saveIntent(viewState.position, intent, isApp)
                update.request(intArrayOf(+appWidgetIdScope))

                if (result.second == CreateShortcutResult.SuccessAppShortcut) {
                    emitAction(ShortcutPickerViewAction.ActivityAction(
                        action = CommonActivityAction.ShowToast(resId = info.anodsplace.carwidget.content.R.string.app_shortcuts_limited)
                    ))
                } else if (result.second == CreateShortcutResult.FailedAppShortcut) {
                    emitAction(ShortcutPickerViewAction.ActivityAction(
                        action = CommonActivityAction.ShowToast(resId = info.anodsplace.carwidget.content.R.string.app_shortcuts_limited, length = Toast.LENGTH_LONG)
                    ))
                }
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }
    }
}
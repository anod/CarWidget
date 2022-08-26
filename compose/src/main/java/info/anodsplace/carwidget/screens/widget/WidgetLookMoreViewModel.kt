package info.anodsplace.carwidget.screens.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.viewmodel.BaseFlowViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

data class WidgetLookMoreState(
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
)

sealed interface WidgetLookMoreEvent {
    class ApplyChange(val key: String, val value: Any?) : WidgetLookMoreEvent
}

sealed interface WidgetLookMoreAction

class WidgetLookMoreViewModel(appWidgetIdScope: AppWidgetIdScope) : BaseFlowViewModel<WidgetLookMoreState, WidgetLookMoreEvent, WidgetLookMoreAction>(), KoinScopeComponent {

    class Factory(private val appWidgetIdScope: AppWidgetIdScope) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WidgetLookMoreViewModel(appWidgetIdScope) as T
        }
    }

    override val scope: Scope = appWidgetIdScope.scope

    private val widgetSettings: WidgetSettings by inject()

    init {
        viewState = WidgetLookMoreState(
            widgetSettings = WidgetInterface.NoOp(widgetSettings)
        )
    }

    override fun handleEvent(event: WidgetLookMoreEvent) {
        when (event) {
            is WidgetLookMoreEvent.ApplyChange -> widgetSettings.applyChange(event.key, event.value)
        }
    }

}
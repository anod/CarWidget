package info.anodsplace.carwidget

import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.main.MainViewModel
import info.anodsplace.viewmodel.BaseFlowViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope

abstract class WidgetAwareViewModel<State, Event, Action>(
    val appWidgetIdScope: AppWidgetIdScope?
) : BaseFlowViewModel<State, Event, Action>(), KoinScopeComponent {

    override val scope: Scope
        get() = appWidgetIdScope?.scope ?: getKoin().getScope(MainViewModel.ROOT_SCOPE_ID)

    override fun onCleared() {
        super.onCleared()
        appWidgetIdScope?.close()
    }
}
package info.anodsplace.carwidget.screens.main

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.isValid
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.WidgetDialog
import info.anodsplace.permissions.AppPermission
import info.anodsplace.viewmodel.BaseFlowViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.scope.Scope

data class MainViewState(
    val isWidget: Boolean,
    val tabs: List<NavItem.Tab>,
    val startRoute: String?,
    val showProDialog: Boolean,
    val topDestination: NavItem,
    val dialogState: WidgetDialog = WidgetDialog.None
)

sealed interface MainViewAction {
    class AppAction(val action: UiAction) : MainViewAction
}

sealed interface MainViewEvent {
    object PermissionAcquired : MainViewEvent
    object HideProDialog : MainViewEvent
    class AppAction(val action: UiAction) : MainViewEvent
}

class MainViewModel(
    val requiredPermissions: List<AppPermission>,
    val appWidgetIdScope: AppWidgetIdScope?,
) : BaseFlowViewModel<MainViewState, MainViewEvent, MainViewAction>(), KoinScopeComponent {
    class Factory(
        private val appWidgetIdScope: AppWidgetIdScope?,
        private val activity: ComponentActivity,
        private val permissionChecker: PermissionChecker,
        private val inCarStatus: InCarStatus
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return MainViewModel(
                if (inCarStatus.isEnabled) permissionChecker.check(activity) else emptyList(),
                appWidgetIdScope
            ) as T
        }
    }

    override val scope: Scope
        get() = appWidgetIdScope?.scope ?: getKoin().getScope(ROOT_SCOPE_ID)

    private val version: Version by inject()
    private val widgetIds: WidgetIds by inject()
    val inCarSettings: InCarInterface by inject()
    val widgetSettings: WidgetInterface

    init {
        val isWidget = appWidgetIdScope.isValid
        val showProDialog = version.isFree && version.isProInstalled
        viewState = MainViewState(
            isWidget = isWidget,
            tabs = listOf(
                if (isWidget) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets,
                NavItem.Tab.InCar,
                NavItem.Tab.About
            ),
            startRoute = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget.Skin.route else null,
            showProDialog = showProDialog,
            topDestination = startDestination(showProDialog)
        )
        widgetSettings = if (isWidget) get() else WidgetInterface.NoOp()
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            MainViewEvent.PermissionAcquired -> {
                viewState = viewState.copy(topDestination = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets)
            }
            MainViewEvent.HideProDialog -> {
                viewState = viewState.copy(showProDialog = false)
            }
            is MainViewEvent.AppAction -> {
                when (val uiAction = event.action) {
                    is UiAction.ShowDialog -> {
                        viewState = viewState.copy(dialogState = uiAction.type)
                    }
                    is UiAction.OnBackNav -> {
                        if (viewState.dialogState != WidgetDialog.None) {
                            viewState = viewState.copy(dialogState = WidgetDialog.None)
                        } else emitAction(MainViewAction.AppAction(UiAction.OnBackNav))
                    }
                    else -> emitAction(MainViewAction.AppAction(event.action))
                }
            }
        }
    }

    private fun startDestination(showProDialog: Boolean): NavItem {
        val isFreeInstalled = !version.isFree && version.isFreeInstalled
        val appWidgetIds = widgetIds.getAllWidgetIds()
        return when {
            appWidgetIds.isEmpty() && !isFreeInstalled -> NavItem.Wizard
            !showProDialog && requiredPermissions.isNotEmpty() -> NavItem.PermissionsRequest
            appWidgetIdScope.isValid -> NavItem.Tab.CurrentWidget
            else -> NavItem.Tab.Widgets
        }
    }

    companion object {
        const val ROOT_SCOPE_ID = "_root_"
    }
}
package info.anodsplace.carwidget.screens.main

import android.appwidget.AppWidgetManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import coil.ImageLoader
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.isValid
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.WidgetAwareViewModel
import info.anodsplace.carwidget.screens.WidgetDialogType
import info.anodsplace.permissions.AppPermission
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject

data class MainViewState(
    val isFreeVersion: Boolean = true,
    val isWidget: Boolean = false,
    val appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    val tabs: List<NavItem.Tab> = emptyList(),
    val startRoute: String? = null,
    val showProDialog: Boolean = false,
    val topDestination: NavItem = NavItem.Tab.About,
    val requiredPermissions: List<AppPermission> = emptyList(),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
)

sealed interface MainViewAction {
    object OnBackNav : MainViewAction
    class ApplyWidget(val appWidgetId: Int, val currentSkinValue: String) : MainViewAction
    class OpenWidgetConfig(val appWidgetId: Int) : MainViewAction
    class ShowDialog(val route: String) : MainViewAction
}

sealed interface MainViewEvent {
    object PermissionAcquired : MainViewEvent
    object HideProDialog : MainViewEvent
    class ShowDialog(val dialogType: WidgetDialogType) : MainViewEvent
    class ApplyWidget(val appWidgetId: Int, val currentSkinValue: String) : MainViewEvent
    class OpenWidgetConfig(val appWidgetId: Int) : MainViewEvent
    class SwitchIconsMono(val isIconsMono: Boolean) : MainViewEvent
    object OnBackNav : MainViewEvent
    object CloseWizard : MainViewEvent
}

class MainViewModel(
    requiredPermissions: List<AppPermission>,
    appWidgetIdScope: AppWidgetIdScope?,
) : WidgetAwareViewModel<MainViewState, MainViewEvent, MainViewAction>(appWidgetIdScope), KoinScopeComponent {

    class Factory(
        private val appWidgetId: Int,
        private val activity: ComponentActivity,
        private val permissionChecker: PermissionChecker,
        private val inCarStatus: InCarStatus
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return MainViewModel(
                requiredPermissions = if (inCarStatus.isEnabled) permissionChecker.check(activity) else emptyList(),
                appWidgetIdScope = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) AppWidgetIdScope(appWidgetId) else null
            ) as T
        }
    }

    private val version: Version by inject()
    private val widgetIds: WidgetIds by inject()
    val imageLoader: ImageLoader by inject()

    init {
        val isWidget = appWidgetIdScope.isValid
        val showProDialog = version.isFree && version.isProInstalled
        viewState = MainViewState(
            isFreeVersion = version.isFree,
            isWidget = isWidget,
            appWidgetId = +appWidgetIdScope,
            tabs = listOf(
                if (isWidget) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets,
                NavItem.Tab.InCar,
                NavItem.Tab.About
            ),
            startRoute = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget.Skin.route else null,
            showProDialog = showProDialog,
            topDestination = startDestination(showProDialog, requiredPermissions, allowWizard = true),
            requiredPermissions = requiredPermissions,
            widgetSettings =  if (isWidget) WidgetInterface.NoOp(get<WidgetSettings>()) else WidgetInterface.NoOp()
        )
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            MainViewEvent.PermissionAcquired -> {
                viewState = viewState.copy(topDestination = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets)
            }
            MainViewEvent.HideProDialog -> {
                viewState = viewState.copy(showProDialog = false)
            }
            is MainViewEvent.ShowDialog -> {
                emitAction(MainViewAction.ShowDialog(NavItem.Tab.CurrentWidget.Skin.Dialog.routeForDialogType(event.dialogType)))
            }
            is MainViewEvent.OnBackNav -> emitAction(MainViewAction.OnBackNav)
            is MainViewEvent.ApplyWidget -> emitAction(MainViewAction.ApplyWidget(event.appWidgetId, event.currentSkinValue))
            is MainViewEvent.OpenWidgetConfig -> emitAction(MainViewAction.OpenWidgetConfig(event.appWidgetId))
            is MainViewEvent.SwitchIconsMono -> {
                val settings = get<WidgetSettings>()
                settings.isIconsMono = event.isIconsMono
                viewState = viewState.copy(widgetSettings = WidgetInterface.NoOp(settings))
            }
            MainViewEvent.CloseWizard -> {
                viewState = viewState.copy(topDestination = startDestination(viewState.showProDialog, viewState.requiredPermissions, allowWizard = false),)
            }
        }
    }

    private fun startDestination(showProDialog: Boolean, requiredPermissions: List<AppPermission>, allowWizard: Boolean): NavItem {
        val isFreeInstalled = !version.isFree && version.isFreeInstalled
        val appWidgetIds = widgetIds.getAllWidgetIds()
        return when {
            appWidgetIds.isEmpty() && !isFreeInstalled &&  allowWizard -> NavItem.Wizard
            !showProDialog && requiredPermissions.isNotEmpty() -> NavItem.PermissionsRequest
            appWidgetIdScope.isValid -> NavItem.Tab.CurrentWidget
            else -> NavItem.Tab.Widgets
        }
    }

    companion object {
        const val ROOT_SCOPE_ID = "_root_"
    }
}
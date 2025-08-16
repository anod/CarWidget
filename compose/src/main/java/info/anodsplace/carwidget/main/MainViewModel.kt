package info.anodsplace.carwidget.main

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.RouteNameSpace
import info.anodsplace.carwidget.SceneNavKey
import info.anodsplace.carwidget.TabNavKey
import info.anodsplace.carwidget.WidgetAwareViewModel
import info.anodsplace.carwidget.appwidget.SkinList
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.getOrCreateAppWidgetScope
import info.anodsplace.carwidget.content.di.isValid
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.framework.content.StartActivityAction
import info.anodsplace.framework.content.forHomeScreen
import info.anodsplace.permissions.AppPermission
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject

@Immutable
data class MainViewState(
    val isWidget: Boolean = false,
    val appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    val tabs: List<TabNavKey> = emptyList(),
    val routeNS: RouteNameSpace = RouteNameSpace.Default,
    val topDestination: SceneNavKey = SceneNavKey.About,
    val requiredPermissions: List<AppPermission> = emptyList(),
    val widgetSettings: WidgetInterface.NoOp = WidgetInterface.NoOp(),
    val skinList: SkinList = SkinList(values = WidgetInterface.skins, titles = WidgetInterface.skins, 0)
)

sealed interface MainViewAction {
    data object OnBackNav : MainViewAction
    class ApplyWidget(val appWidgetId: Int, val currentSkinValue: String) : MainViewAction
    class OpenWidgetConfig(val appWidgetId: Int) : MainViewAction
    class ShowDialog(val route: String) : MainViewAction
    class StartActivity(override val intent: Intent) : MainViewAction, StartActivityAction
}

sealed interface MainViewEvent {
    data object PermissionAcquired : MainViewEvent
    class ApplyWidget(val appWidgetId: Int, val currentSkinValue: String) : MainViewEvent
    class OpenWidgetConfig(val appWidgetId: Int) : MainViewEvent
    class WidgetUpdateSkin(val skinIdx: Int) : MainViewEvent

    data object OnBackNav : MainViewEvent
    data object CloseWizard : MainViewEvent
    data object ShowWizard : MainViewEvent
    data object GotToHomeScreen : MainViewEvent
}

class MainViewModel(
    requiredPermissions: List<AppPermission>,
    appWidgetIdScope: AppWidgetIdScope?,
    private val appSettings: AppSettings
) : WidgetAwareViewModel<MainViewState, MainViewEvent, MainViewAction>(appWidgetIdScope), KoinScopeComponent {

    class Factory(
        private val appWidgetId: Int,
        private val activity: ComponentActivity,
        private val permissionChecker: PermissionChecker,
        private val appSettings: AppSettings
    ) : ViewModelProvider.Factory, KoinComponent {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return MainViewModel(
                requiredPermissions = if (appSettings.checkPermissionsOnStart) permissionChecker.check(activity) else emptyList(),
                appWidgetIdScope = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) getOrCreateAppWidgetScope(appWidgetId) else null,
                appSettings = appSettings
            ) as T
        }
    }

    private val widgetIds: WidgetIds by inject()
    val imageLoader: ImageLoader by inject()

    init {
        val isWidget = appWidgetIdScope.isValid
        val widgetSettings = if (isWidget) WidgetInterface.NoOp(get<WidgetSettings>()) else WidgetInterface.NoOp()
        viewState = MainViewState(
            isWidget = isWidget,
            appWidgetId = +appWidgetIdScope,
            tabs = if (isWidget) listOf(
                SceneNavKey.CurrentWidget,
                SceneNavKey.WidgetCustomize,
                SceneNavKey.InCar,
                SceneNavKey.About
            ) else listOf(
                SceneNavKey.Widgets,
                SceneNavKey.InCar,
                SceneNavKey.About
            ),
            routeNS = if (isWidget) RouteNameSpace.AppWidget else RouteNameSpace.Default,
            topDestination = startDestination(requiredPermissions, allowWizard = true),
            requiredPermissions = requiredPermissions,
            widgetSettings = widgetSettings,
            skinList = SkinList(widgetSettings.skin, get())
        )
        viewModelScope.launch {
            widgetSettings.observe<String>(WidgetSettings.SKIN).collect { skin ->
                viewState = viewState.copy(skinList = viewState.skinList.copy(selectedSkinPosition = WidgetInterface.skins.indexOf(skin)))
            }
        }
    }

    override fun handleEvent(event: MainViewEvent) {
        when (event) {
            is MainViewEvent.PermissionAcquired -> {
                appSettings.checkPermissionsOnStart = false
                viewState = viewState.copy(topDestination = if (appWidgetIdScope.isValid) SceneNavKey.CurrentWidget else SceneNavKey.Widgets)
            }
            is MainViewEvent.OnBackNav -> emitAction(MainViewAction.OnBackNav)
            is MainViewEvent.ApplyWidget -> emitAction(
                MainViewAction.ApplyWidget(
                    event.appWidgetId,
                    event.currentSkinValue
                )
            )
            is MainViewEvent.OpenWidgetConfig -> emitAction(MainViewAction.OpenWidgetConfig(event.appWidgetId))
            is MainViewEvent.ShowWizard -> {
                viewState = viewState.copy(topDestination = SceneNavKey.Wizard)
            }
            is MainViewEvent.CloseWizard -> {
                viewState = viewState.copy(topDestination = startDestination(viewState.requiredPermissions, allowWizard = false))
            }
            is MainViewEvent.GotToHomeScreen -> {
                emitAction(
                    MainViewAction.StartActivity(
                        intent =  Intent().forHomeScreen().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                )
            }
            is MainViewEvent.WidgetUpdateSkin -> {
                val newList = viewState.skinList.copy(selectedSkinPosition = event.skinIdx)
                if (scope.closed) {
                    AppLog.e("Scope is closed when calling WidgetUpdateSkin")
                } else {
                    val settings = get<WidgetSettings>()
                    settings.skin = newList.current.value
                }
                viewState = viewState.copy(skinList = newList)
            }
        }
    }

    private fun startDestination(requiredPermissions: List<AppPermission>, allowWizard: Boolean): SceneNavKey {
        val appWidgetIds = widgetIds.getAllWidgetIds()
        return when {
            appWidgetIds.isEmpty() && allowWizard -> SceneNavKey.Wizard
            requiredPermissions.isNotEmpty() -> SceneNavKey.PermissionsRequest
            appWidgetIdScope.isValid -> SceneNavKey.CurrentWidget
            else -> SceneNavKey.Widgets
        }
    }

    companion object {
        const val ROOT_SCOPE_ID = "_root_"
    }
}
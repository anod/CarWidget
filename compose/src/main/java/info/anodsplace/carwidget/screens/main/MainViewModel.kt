package info.anodsplace.carwidget.screens.main

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
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
import info.anodsplace.permissions.AppPermission
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.scope.Scope

class MainViewModel(
    val requiredPermissions: List<AppPermission>,
    val appWidgetIdScope: AppWidgetIdScope?,
    application: Application
) : AndroidViewModel(application), KoinScopeComponent {
    class Factory(
        private val appWidgetIdScope: AppWidgetIdScope?,
        private val activity: ComponentActivity,
        private val permissionChecker: PermissionChecker,
        private val inCarStatus: InCarStatus
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return MainViewModel(
                if (inCarStatus.isEnabled) permissionChecker.check(activity) else emptyList(),
                appWidgetIdScope,
                activity.applicationContext as Application
            ) as T
        }
    }

    override val scope: Scope
        get() = appWidgetIdScope?.scope ?: getKoin().getScope(ROOT_SCOPE_ID)

    private val version: Version by inject()
    private val widgetIds: WidgetIds by inject()
    val inCarSettings: InCarInterface by inject()

    val isWidget = appWidgetIdScope.isValid
    val tabs: List<NavItem.Tab> = listOf(
        if (isWidget) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets,
        NavItem.Tab.InCar,
        NavItem.Tab.About
    )
    val widgetSettings: WidgetInterface = if (isWidget) get() else WidgetInterface.NoOp()
    val startRoute: String? = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget.Skin.route else null
    val action = MutableSharedFlow<UiAction>()
    val showProDialog = mutableStateOf(version.isFree && version.isProInstalled)
    val topDestination: MutableState<NavItem> = mutableStateOf(startDestination())

    private fun startDestination(): NavItem {
        val isFreeInstalled = !version.isFree && version.isFreeInstalled
        val appWidgetIds = widgetIds.getAllWidgetIds()
        return when {
            appWidgetIds.isEmpty() && !isFreeInstalled -> NavItem.Wizard
            !showProDialog.value && requiredPermissions.isNotEmpty() -> NavItem.PermissionsRequest
            appWidgetIdScope.isValid -> NavItem.Tab.CurrentWidget
            else -> NavItem.Tab.Widgets
        }
    }

    fun onPermissionsAcquired() {
        topDestination.value = if (appWidgetIdScope.isValid) NavItem.Tab.CurrentWidget else NavItem.Tab.Widgets
    }

    companion object {
        const val ROOT_SCOPE_ID = "_root_"
    }
}
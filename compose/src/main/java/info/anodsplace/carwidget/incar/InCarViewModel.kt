package info.anodsplace.carwidget.incar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavHostController
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.SceneNavKey
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.carwidget.utils.toPermissionDescription
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.content.ShowToastActionDefaults
import info.anodsplace.framework.content.forLauncher
import info.anodsplace.framework.content.showToast
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@Immutable
data class InCarViewState(
    val items: List<PreferenceItem> = emptyList(),
    val notificationShortcuts: Map<Int, Shortcut?>? = null,
    val inCar: InCarInterface.NoOp = InCarInterface.NoOp(),
    val missingPermissionsDialog: List<PermissionDescription> = emptyList(),
    val requiredPermissionsNotice: List<PermissionDescription> = emptyList(),
)

sealed interface InCarViewEvent {
    class Navigate(val route: SceneNavKey) : InCarViewEvent
    class SaveScreenTimeout(val disabled: Boolean, val disableCharging: Boolean) : InCarViewEvent
    class ToggleScreenAlert(val useAlert: Boolean) : InCarViewEvent
    class ApplyChange(val key: String, val value: Any?) : InCarViewEvent
    class NotificationShortcutUpdate(val shortcutIndex: Int, val entry: ChooserEntry) : InCarViewEvent
    class SetAutorunApp(val componentName: ComponentName?) : InCarViewEvent
    class RequestPermissionResult(val result: List<AppPermission>, val missingPermissions: List<PermissionDescription>, val activity: ComponentActivity, val ex: Exception?) : InCarViewEvent
    class RequestPermission(val missingPermissions: List<PermissionDescription>) : InCarViewEvent
}

sealed interface InCarViewAction {
    class Navigate(val route: SceneNavKey) : InCarViewAction
    class RequestPermissions(val permissions: List<AppPermission>) : InCarViewAction
    class CheckPermissions(val permissions: List<AppPermission>) : InCarViewAction {
        constructor(permission: AppPermission) : this(listOf(permission))
    }
    class ShowToast(text: String) : ShowToastActionDefaults(text = text), InCarViewAction
    data object CheckMissingPermissions : InCarViewAction
}

fun InCarInterface.saveScreenTimeout(disabled: Boolean, disableCharging: Boolean) {
    isDisableScreenTimeout = disabled
    isDisableScreenTimeoutCharging = disableCharging
    applyPending()
}

class InCarViewModel(
    private val context: Context,
    private val database: ShortcutsDatabase,
    private val inCar: InCarSettings,
    private val permissionChecker: PermissionChecker,
    private val permissionDescriptionsMap: Map<AppPermission, PermissionDescription>,
    initialMissingPermissions: List<AppPermission>
) : BaseFlowViewModel<InCarViewState, InCarViewEvent, InCarViewAction>()  {

    private val shortcutsModel = NotificationShortcutsModel(context, database)

    val appsLoader: ChooserLoader
        get() = QueryIntentLoader(context, Intent().forLauncher())

    class Factory(
        private val activity: ComponentActivity,
    ) : ViewModelProvider.Factory, KoinComponent {
        private val context: Context by inject()
        private val database: ShortcutsDatabase by inject()
        private val inCar: InCarSettings by inject()
        private val permissionChecker: PermissionChecker by inject()
        private val permissionDescriptions: List<PermissionDescriptionItem> by inject(named("permissionDescriptions"))

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val permissionDescriptionsMap = permissionDescriptions
                .associateBy({ AppPermissions.fromValue(it.permission)}, { it.toPermissionDescription() })
            return InCarViewModel(
                context = context,
                database = database,
                inCar = inCar,
                permissionChecker = permissionChecker,
                permissionDescriptionsMap = permissionDescriptionsMap,
                initialMissingPermissions = permissionChecker.check(activity)
            ) as T
        }
    }

    init {
        viewState = InCarViewState(
            items = createCarScreenItems(inCar = inCar, context = context),
            inCar = InCarInterface.NoOp(inCar),
            requiredPermissionsNotice = initialMissingPermissions.mapNotNull { permissionDescriptionsMap[it] }
        )

        viewModelScope.launch {
            shortcutsModel.init()
            viewState = viewState.copy(notificationShortcuts = shortcutsModel.shortcuts)
        }

        viewModelScope.launch {
            inCar.changes.collect {
                viewState = viewState.copy(
                    items = createCarScreenItems(inCar = inCar, context = context),
                    inCar = InCarInterface.NoOp(inCar)
                )
            }
        }

        viewModelScope.launch {
            database.observeTarget(NotificationShortcutsModel.notificationTargetId).collect {
                shortcutsModel.init()
                viewState = viewState.copy(notificationShortcuts = shortcutsModel.shortcuts)
            }
        }
    }

    override fun handleEvent(event: InCarViewEvent) {
        when (event) {
            is InCarViewEvent.SaveScreenTimeout -> {
                inCar.saveScreenTimeout(
                    disabled = event.disabled,
                    disableCharging = event.disableCharging,
                )
            }
            is InCarViewEvent.Navigate -> emitAction(InCarViewAction.Navigate(event.route))
            is InCarViewEvent.ToggleScreenAlert -> toggleScreenAlert(event.useAlert)
            is InCarViewEvent.ApplyChange -> applyChange(event.key, event.value)
            is InCarViewEvent.NotificationShortcutUpdate -> updateShortcut(event.shortcutIndex, event.entry)
            is InCarViewEvent.SetAutorunApp -> inCar.autorunApp = event.componentName
            is InCarViewEvent.RequestPermissionResult -> {
                val requiredPermissions = permissionChecker.check(event.missingPermissions.map { it.permission }, event.activity)
                viewState = viewState.copy(
                    missingPermissionsDialog = emptyList(),
                    requiredPermissionsNotice = requiredPermissions.mapNotNull { permissionDescriptionsMap[it] }
                )
                if (event.ex != null) {
                    AppLog.e(event.ex)
                    emitAction(InCarViewAction.ShowToast(text = "An error occurred during requesting permission"))
                }
            }

            is InCarViewEvent.RequestPermission -> viewState = viewState.copy(
                missingPermissionsDialog = event.missingPermissions,
            )
        }
    }

    private fun applyChange(key: String, value: Any?) {
        inCar.applyChange(key, value)

        when (key) {
            InCarSettings.SCREEN_ORIENTATION -> emitAction(InCarViewAction.CheckPermissions(permission = AppPermission.CanDrawOverlay))
            InCarSettings.BRIGHTNESS -> emitAction(InCarViewAction.CheckPermissions(permission = AppPermission.WriteSettings))
            InCarSettings.AUTO_ANSWER -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                emitAction(InCarViewAction.CheckPermissions(permissions = listOf(AppPermission.AnswerPhoneCalls, AppPermission.PhoneStateRead)))
            } else {
                emitAction(InCarViewAction.CheckPermissions(permission = AppPermission.AnswerPhoneCalls))
            }
            InCarSettings.ACTIVITY_RECOGNITION -> emitAction(InCarViewAction.CheckPermissions(permission = AppPermission.ActivityRecognition))
            InCarSettings.INCAR_MODE_ENABLED -> emitAction(InCarViewAction.CheckMissingPermissions)
        }
    }

    private fun toggleScreenAlert(useAlert: Boolean) {
        if (useAlert) {
            inCar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(true, inCar.screenOnAlert)
            emitAction(InCarViewAction.CheckPermissions(AppPermission.CanDrawOverlay))
        } else {
            inCar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(false, inCar.screenOnAlert)
        }
        inCar.applyPending()
    }

    private fun updateShortcut(shortcutIndex: Int, entry: ChooserEntry) {
        viewModelScope.launch {
            if (entry.componentName == null) {
                shortcutsModel.drop(shortcutIndex)
            } else {
                shortcutsModel.saveIntent(
                    shortcutIndex,
                    entry.getIntent(baseIntent = null),
                    isApplicationShortcut = true
                )
            }
        }
    }

    fun handleAction(action: InCarViewAction, navController: NavHostController, activity: ComponentActivity) {
        when (action) {
            is InCarViewAction.Navigate -> navController.navigate(action.route)
            is InCarViewAction.CheckPermissions -> {
                val required = permissionChecker.check(action.permissions, activity).mapNotNull { permissionDescriptionsMap[it] }
                if (required.isNotEmpty()) {
                    handleEvent(InCarViewEvent.RequestPermission(missingPermissions = required))
                } else if (viewState.requiredPermissionsNotice.isNotEmpty()) {
                    viewState = viewState.copy(
                        requiredPermissionsNotice = permissionChecker.check(activity).mapNotNull { permissionDescriptionsMap[it] }
                    )
                }
            }
            is InCarViewAction.RequestPermissions -> viewState = viewState.copy(
                missingPermissionsDialog = action.permissions.mapNotNull { permissionDescriptionsMap[it] }
            )
            InCarViewAction.CheckMissingPermissions -> {
                viewState = viewState.copy(
                    requiredPermissionsNotice = permissionChecker.check(activity).mapNotNull { permissionDescriptionsMap[it] }
                )
            }
            is InCarViewAction.ShowToast -> activity.showToast(action)
        }
    }
}
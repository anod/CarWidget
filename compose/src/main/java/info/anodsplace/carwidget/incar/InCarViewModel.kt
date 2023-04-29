package info.anodsplace.carwidget.incar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarSettings
import info.anodsplace.carwidget.content.shortcuts.NotificationShortcutsModel
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.content.forLauncher
import info.anodsplace.framework.content.forOverlayPermission
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.viewmodel.BaseFlowViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Immutable
data class InCarViewState(
    val items: List<PreferenceItem> = emptyList(),
    val notificationShortcuts: Map<Int, Shortcut?>? = null,
    val inCar: InCarInterface.NoOp = InCarInterface.NoOp()
)

sealed interface InCarViewEvent {
    class Navigate(val route: String) : InCarViewEvent
    class SaveScreenTimeout(val disabled: Boolean, val disableCharging: Boolean) : InCarViewEvent
    class ToggleScreenAlert(val useAlert: Boolean) : InCarViewEvent
    class ApplyChange(val key: String, val value: Any?) : InCarViewEvent
    class NotificationShortcutUpdate(val shortcutIndex: Int, val entry: ChooserEntry) :
        InCarViewEvent
    class SetAutorunApp(val componentName: ComponentName?) : InCarViewEvent
}

sealed interface InCarViewAction {
    class Navigate(val route: String) : InCarViewAction
    class StartActivity(val intent: Intent) : InCarViewAction
}

fun InCarInterface.saveScreenTimeout(disabled: Boolean, disableCharging: Boolean) {
    isDisableScreenTimeout = disabled
    isDisableScreenTimeoutCharging = disableCharging
    applyPending()
}

class InCarViewModel : BaseFlowViewModel<InCarViewState, InCarViewEvent, InCarViewAction>(), KoinComponent {
    private val context: Context by inject()
    private val database: ShortcutsDatabase by inject()
    private val inCar: InCarSettings by inject()
    private val shortcutsModel = NotificationShortcutsModel(context, database)

    val appsLoader: ChooserLoader
        get() = QueryIntentLoader(context, Intent().forLauncher())

    init {
        viewState = InCarViewState(
            items = createCarScreenItems(inCar = inCar, context = context),
            inCar = InCarInterface.NoOp(inCar)
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
            is InCarViewEvent.ApplyChange -> inCar.applyChange(event.key, event.value)
            is InCarViewEvent.NotificationShortcutUpdate -> updateShortcut(event.shortcutIndex, event.entry)
            is InCarViewEvent.SetAutorunApp -> inCar.autorunApp = event.componentName
        }
    }

    private fun toggleScreenAlert(useAlert: Boolean) {
        if (useAlert) {
            inCar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(true, inCar.screenOnAlert)
            if (!AlertWindow.hasPermission(context)) {
                emitAction(
                    InCarViewAction.StartActivity(
                        intent = Intent().forOverlayPermission(
                            context.packageName
                        )
                    )
                )
            }
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

    fun handleAction(action: InCarViewAction, navController: NavHostController, activityContext: Context) {
        when (action) {
            is InCarViewAction.Navigate -> navController.navigate(action.route)
            is InCarViewAction.StartActivity -> activityContext.startActivitySafely(action.intent)
        }
    }
}
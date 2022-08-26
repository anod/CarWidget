package info.anodsplace.carwidget.screens.incar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.StaticChooserLoader
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.compose.checked
import info.anodsplace.compose.value

@Composable
fun InCarMainScreen(
    screenState: InCarViewState,
    modifier: Modifier = Modifier,
    onEvent: (InCarViewEvent) -> Unit = { },
    appsLoader: ChooserLoader = StaticChooserLoader(emptyList()),
) {
    var appChooser: PreferenceItem? by remember { mutableStateOf(null) }

    Surface {
        PreferencesScreen(
            preferences = screenState.items,
            modifier = modifier,
            categoryColor = MaterialTheme.colorScheme.secondary,
            descriptionColor = MaterialTheme.colorScheme.onBackground,
            onClick = { item ->
                when (item.key) {
                    "bt-device-screen" -> {
                        onEvent(InCarViewEvent.Navigate(route = NavItem.Tab.InCar.Bluetooth.route))
                    }
                    "screen-timeout-list" -> {
                        onEvent(InCarViewEvent.SaveScreenTimeout(
                            disabled = item.checked,
                            disableCharging = screenState.inCar.isDisableScreenTimeoutCharging
                        ))
                    }
                   "autorun-app-choose" -> {
                        if (item.value == "disabled") {
                            onEvent(InCarViewEvent.SetAutorunApp(componentName = null))
                        } else {
                            appChooser = item
                        }
                    }
                    else -> onPreferenceClick(item, onEvent = onEvent)
                }
            },
            placeholder = { item, paddingValues ->
                when (item.key) {
                    "notif-shortcuts" -> {
                        Box(modifier = Modifier.padding(paddingValues)) {
                            NotificationShortcuts(screenState = screenState, appsLoader = appsLoader)
                        }
                    }
                    "screen-timeout-list" -> {
                        if (item.checked) {
                            ScreenTimeoutContent(screenState = screenState, onEvent = onEvent)
                        }
                    }
                    "adjust-volume-level" -> {
                        if (item.checked) {
                            MediaSettings(screenState = screenState, onEvent = onEvent)
                        }
                    }
                    else -> {}
                }
            }
        )
    }

    if (appChooser != null) {
        ChooserDialog(
            modifier = Modifier.fillMaxHeight(fraction = 0.8f),
            loader = appsLoader,
            onDismissRequest = {
                appChooser = null
            },
            onClick = {
                onEvent(InCarViewEvent.SetAutorunApp(componentName = it.componentName))
                appChooser = null
            }
        )
    }
}

fun onPreferenceClick(
    item: PreferenceItem,
    onEvent: (InCarViewEvent) -> Unit = { },
) {
    when (item) {
        is PreferenceItem.Category -> { }
        is PreferenceItem.CheckBox -> {
            onEvent(InCarViewEvent.ApplyChange(item.key, item.checked))
        }
        is PreferenceItem.Switch -> {
            onEvent(InCarViewEvent.ApplyChange(item.key, item.checked))
        }
        is PreferenceItem.List -> {
            onEvent(InCarViewEvent.ApplyChange(item.key, item.value))
        }
        is PreferenceItem.Text -> { }
        is PreferenceItem.Placeholder -> { }
        is PreferenceItem.Pick -> { }
    }
}

@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme {
        InCarMainScreen(
            screenState = InCarViewState()
        )
    }
}
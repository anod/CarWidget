package info.anodsplace.carwidget.incar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.NavItem
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.StaticChooserLoader
import info.anodsplace.carwidget.content.R
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesDefaults
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.compose.RequestPermissionsScreen
import info.anodsplace.compose.RequestPermissionsScreenDescription
import info.anodsplace.compose.checked
import info.anodsplace.compose.findActivity
import info.anodsplace.compose.key
import info.anodsplace.compose.value

@Composable
fun InCarMainScreen(
    screenState: InCarViewState,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onEvent: (InCarViewEvent) -> Unit = { },
    appsLoader: ChooserLoader = StaticChooserLoader(emptyList()),
    imageLoader: ImageLoader
) {
    var appChooser: PreferenceItem? by remember { mutableStateOf(null) }

    Surface {
        PreferencesScreen(
            preferences = screenState.items,
            modifier = Modifier.padding(innerPadding),
            colors = PreferencesDefaults.colors(
                descriptionColor = MaterialTheme.colorScheme.onBackground,
            ),
            onClick = { item ->
                when (item.key) {
                    "bt-device-screen" -> {
                        onEvent(InCarViewEvent.Navigate(route = NavItem.Tab.InCar.Bluetooth.route))
                    }
                    "screen-timeout-list" -> {
                        onEvent(
                            InCarViewEvent.SaveScreenTimeout(
                                disabled = item.checked,
                                disableCharging = screenState.inCar.isDisableScreenTimeoutCharging
                            )
                        )
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
                            NotificationShortcuts(screenState = screenState, appsLoader = appsLoader, imageLoader = imageLoader)
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
                    "incar-mode-enabled" -> {
                        if (screenState.requiredPermissionsNotice.isNotEmpty()) {
                            MissingPermissionNotice(
                                missingPermissions = screenState.requiredPermissionsNotice,
                                onEvent = onEvent
                            )
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
            },
            imageLoader = imageLoader
        )
    }

    if (screenState.missingPermissionsDialog.isNotEmpty()) {
        RequestPermissionsDialog(
            missingPermissions = screenState.missingPermissionsDialog,
            onEvent = onEvent
        )
    }
}

@Composable
private  fun MissingPermissionNotice(
    missingPermissions: List<PermissionDescription>,
    onEvent: (InCarViewEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .clip(shape = MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.missing_required_permissions),
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = { onEvent(InCarViewEvent.RequestPermission(missingPermissions = missingPermissions)) }
        ) {
            Text(text = stringResource(R.string.show))
        }
    }
}

@Composable
private fun RequestPermissionsDialog(
    missingPermissions: List<PermissionDescription>,
    onEvent: (InCarViewEvent) -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = {
            onEvent(InCarViewEvent.RequestPermissionResult(emptyList(), missingPermissions, context.findActivity()))
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface {
            RequestPermissionsScreen(
                modifier = Modifier.padding(16.dp),
                input = missingPermissions.map { it },
                screenDescription = RequestPermissionsScreenDescription(
                    titleRes = R.string.missing_required_permissions,
                    allowAccessRes = R.string.allow_access,
                    cancelRes = android.R.string.cancel
                ),
                onResult = { result ->
                    onEvent(InCarViewEvent.RequestPermissionResult(result, missingPermissions, context.findActivity()))
                }
            )
        }
    }
}

private fun onPreferenceClick(
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
        is PreferenceItem.Pick -> {
            onEvent(InCarViewEvent.ApplyChange(item.key, item.value))
        }
        is PreferenceItem.Color -> onEvent(InCarViewEvent.ApplyChange(item.key, item.color))
        is PreferenceItem.Spacer -> {}
    }
}

@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme {
        InCarMainScreen(
            screenState = InCarViewState(),
            imageLoader = ImageLoader.Builder(LocalContext.current).build()
        )
    }
}
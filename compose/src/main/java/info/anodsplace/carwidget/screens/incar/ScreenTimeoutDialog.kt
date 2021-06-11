package info.anodsplace.carwidget.screens.incar

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.content.forOverlayPermission
import info.anodsplace.framework.content.startActivitySafely

fun InCarInterface.saveScreenTimeout(disabled: Boolean, disableCharging: Boolean) {
    isDisableScreenTimeout = disabled
    isDisableScreenTimeoutCharging = disableCharging
    apply()
}

@Composable
fun ScreenTimeoutContent(inCar: InCarInterface) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        var isDisableScreenTimeout by remember { mutableStateOf(inCar.isDisableScreenTimeout) }
        var isDisableScreenTimeoutCharging by remember { mutableStateOf(inCar.isDisableScreenTimeoutCharging) }
        var screenOnAlertEnabled by remember { mutableStateOf(inCar.screenOnAlert.enabled) }

        PreferenceSwitch(
            checked = isDisableScreenTimeout,
            item = PreferenceItem.Text(titleRes = R.string.keep_on)
        ) {
            inCar.saveScreenTimeout(
                disabled = it,
                disableCharging = inCar.isDisableScreenTimeoutCharging,
            )
            isDisableScreenTimeout = it
        }
        PreferenceCheckbox(
            checked = isDisableScreenTimeoutCharging,
            item = PreferenceItem.Text(titleRes = R.string.while_charging)
        ) {
            inCar.saveScreenTimeout(
                disabled = inCar.isDisableScreenTimeout,
                disableCharging = it,
            )
            isDisableScreenTimeoutCharging = it
        }
        PreferenceCheckbox(
            checked = screenOnAlertEnabled,
            item = PreferenceItem.Text(
                titleRes = R.string.screen_on_alternative,
                summaryRes = R.string.screen_on_alternative_text
            )
        ) { useAlert ->
            screenOnAlertEnabled = useAlert
            if (useAlert) {
                inCar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(true, inCar.screenOnAlert)
                if (!AlertWindow.hasPermission(context)) {
                    context.startActivitySafely(Intent().forOverlayPermission(context.packageName))
                }
            } else {
                inCar.screenOnAlert = InCarInterface.ScreenOnAlertSettings(false, inCar.screenOnAlert)
            }
            inCar.apply()
        }
    }
}

@Composable
fun ScreenTimeoutDialog(item: PreferenceItem.Text, inCar: InCarInterface, onDismiss: () -> Unit) {
    AlertDialog(
        title = { Text(text = if (item.titleRes != 0) stringResource(id = item.titleRes) else item.title) },
        text = { ScreenTimeoutContent(inCar) },
        buttons = { },
        onDismissRequest = {
            onDismiss()
        }
    )
}

@Preview("ScreenTimeout Dark")
@Composable
fun ScreenTimeoutDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            ScreenTimeoutContent(
                inCar = InCarInterface.NoOp()
            )
        }
    }
}
package info.anodsplace.carwidget.screens.incar

import android.app.UiModeManager
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.PreferenceCheckbox
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.app.AlertWindow
import info.anodsplace.framework.content.forOverlayPermission
import info.anodsplace.framework.content.startActivitySafely

fun InCarInterface.saveScreenTimeout(disabled: Boolean, disableCharging: Boolean) {
    isDisableScreenTimeout = disabled
    isDisableScreenTimeoutCharging = disableCharging
    applyPending()
}

@Composable
fun ScreenTimeoutContent(inCar: InCarInterface) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        var isDisableScreenTimeoutCharging by remember { mutableStateOf(inCar.isDisableScreenTimeoutCharging) }
        var screenOnAlertEnabled by remember { mutableStateOf(inCar.screenOnAlert.enabled) }

        PreferenceCheckbox(
            checked = isDisableScreenTimeoutCharging,
            item = PreferenceItem.Text(titleRes = R.string.while_charging),
            checkBoxColor = MaterialTheme.colorScheme.onSurface,
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
            ),
            checkBoxColor = MaterialTheme.colorScheme.onSurface,
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
            inCar.applyPending()
        }
    }
}


@Preview("ScreenTimeout Dark")
@Composable
fun ScreenTimeoutDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        BackgroundSurface {
            ScreenTimeoutContent(
                inCar = InCarInterface.NoOp()
            )
        }
    }
}
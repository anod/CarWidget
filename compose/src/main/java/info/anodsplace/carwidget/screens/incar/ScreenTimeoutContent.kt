package info.anodsplace.carwidget.screens.incar

import android.app.UiModeManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferenceSwitch

@Composable
fun ScreenTimeoutContent(
    screenState: InCarViewState,
    onEvent: (InCarViewEvent) -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        var isDisableScreenTimeoutCharging by remember { mutableStateOf(screenState.inCar.isDisableScreenTimeoutCharging) }
        var screenOnAlertEnabled by remember { mutableStateOf(screenState.inCar.screenOnAlert.enabled) }

        PreferenceSwitch(
            checked = isDisableScreenTimeoutCharging,
            item = PreferenceItem.Text(titleRes = info.anodsplace.carwidget.content.R.string.while_charging)) {
            onEvent(InCarViewEvent.SaveScreenTimeout(
                disabled = screenState.inCar.isDisableScreenTimeout,
                disableCharging = it
            ))
            isDisableScreenTimeoutCharging = it
        }
        PreferenceSwitch(
            checked = screenOnAlertEnabled,
            item = PreferenceItem.Text(
                titleRes = info.anodsplace.carwidget.content.R.string.screen_on_alternative,
                summaryRes = info.anodsplace.carwidget.content.R.string.screen_on_alternative_text
            ),
        ) { useAlert ->
            screenOnAlertEnabled = useAlert
            onEvent(InCarViewEvent.ToggleScreenAlert(useAlert))
        }
    }
}


@Preview("ScreenTimeout Dark")
@Composable
fun ScreenTimeoutDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        Surface {
            ScreenTimeoutContent(
                screenState = InCarViewState()
            )
        }
    }
}
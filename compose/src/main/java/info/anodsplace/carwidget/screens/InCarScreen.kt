package info.anodsplace.carwidget.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.preferences.createCarScreenItems
import info.anodsplace.carwidget.screens.incar.NotificationShortcuts
import info.anodsplace.carwidget.screens.incar.ScreenTimeoutDialog

@Composable
fun InCarMainScreen(
    inCar: InCarInterface,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier) {

    var screenTimeout: PreferenceItem.Text? by remember { mutableStateOf(null) }

    PreferencesScreen(
        preferences = createCarScreenItems(inCar),
        modifier = modifier,
        onClick = { item -> onPreferenceClick(item, inCar) { textItem ->
            when (textItem.key) {
                "bt-device-screen" -> {
                    navController.navigate(NavItem.InCar.Bluetooth.route) { }
                }
                "screen-timeout-list" -> {
                    screenTimeout = textItem
                }
                "media-screen" -> {
                    navController.navigate(NavItem.InCar.Media.route) { }
                }
                "more-screen" -> {
                    navController.navigate(NavItem.InCar.More.route) { }
                }
            }
        } },
        placeholder = { item ->
            when (item.key) {
                "notif-shortcuts" -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        NotificationShortcuts(inCar = inCar)
                    }
                }
                else -> {}
            }
        }
    )

    if (screenTimeout != null) {
        ScreenTimeoutDialog(item = screenTimeout!!, inCar = inCar) {
            screenTimeout = null
        }
    }
}

fun onPreferenceClick(
    item: PreferenceItem,
    inCar: InCarInterface,
    navigate: (item: PreferenceItem.Text) -> Unit
) {
    when (item) {
        is PreferenceItem.Category -> { }
        is PreferenceItem.CheckBox -> {
            inCar.applyChange(item.key, item.checked)
        }
        is PreferenceItem.Switch -> {
            inCar.applyChange(item.key, item.checked)
        }
        is PreferenceItem.List -> {
            inCar.applyChange(item.key, item.value)
        }
        is PreferenceItem.Text -> { navigate(item) }
    }
}

@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            InCarMainScreen(InCarInterface.NoOp())
        }
    }
}

@Preview("InCarScreen Dark")
@Composable
fun InCarScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            InCarMainScreen(InCarInterface.NoOp())
        }
    }
}
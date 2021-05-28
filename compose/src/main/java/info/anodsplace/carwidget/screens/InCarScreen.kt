package info.anodsplace.carwidget.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.compose.*
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.preferences.createCarScreenItems
import info.anodsplace.carwidget.screens.incar.ScreenTimeout

@Composable
fun InCarMainScreen(
    inCar: InCarInterface,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier) {

    var screenTimeout: PreferenceItem.Text? by remember { mutableStateOf(null) }

    PreferencesScreen(
        preferences = createCarScreenItems(inCar),
        modifier = modifier,
        onClick = { item ->
        when (item) {
            is PreferenceItem.Category -> { }
            is PreferenceItem.CheckBox -> {
                inCar.putChange(item.key, item.checked)
            }
            is PreferenceItem.Switch -> {
                inCar.putChange(item.key, item.checked)
            }
            is PreferenceItem.List -> {
                inCar.putChange(item.key, item.value)
            }
            is PreferenceItem.Text -> {
                if (item.key == "bt-device-screen") {
                    navController.navigate(NavItem.InCar.Bluetooth.route) { }
                } else if (item.key == "screen-timeout-list") {
                    screenTimeout = item
                }
            }
        }
    })

    if (screenTimeout != null) {
        ScreenTimeout(item = screenTimeout!!, inCar = inCar) {
            screenTimeout = null
        }
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
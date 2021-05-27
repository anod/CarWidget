package info.anodsplace.carwidget.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.PreferenceItem
import info.anodsplace.carwidget.compose.PreferencesScreen
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.preferences.createCarScreenItems

@Composable
fun InCarMainScreen(
    inCar: InCarInterface,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier) {

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
                }
            }
        }
    })

}

@Composable
fun InCarBluetoothScreen(inCar: InCarInterface, modifier: Modifier) {


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
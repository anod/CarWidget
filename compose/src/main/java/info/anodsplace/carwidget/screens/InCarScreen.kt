package info.anodsplace.carwidget.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.PreferenceItem
import info.anodsplace.carwidget.compose.PreferencesScreen
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.preferences.createCarScreenItems

@Composable
fun InCarScreen(inCar: InCarInterface, modifier: Modifier = Modifier) {

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

            }
        }
    })

}

@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            InCarScreen(InCarInterface.NoOp())
        }
    }
}

@Preview("InCarScreen Dark")
@Composable
fun InCarScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            InCarScreen(InCarInterface.NoOp())
        }
    }
}
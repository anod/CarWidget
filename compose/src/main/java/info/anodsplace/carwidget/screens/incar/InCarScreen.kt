package info.anodsplace.carwidget.screens.incar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen

@Composable
fun InCarMainScreen(
    viewModel: InCarViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()) {

    PreferencesScreen(
        preferences = viewModel.items,
        modifier = modifier,
        categoryColor = MaterialTheme.colorScheme.secondary,
        descriptionColor = MaterialTheme.colorScheme.onBackground,
        onClick = { item ->
            when (item.key) {
                "bt-device-screen" -> {
                    navController.navigate(NavItem.Tab.InCar.Bluetooth.route) { }
                }
                "media-screen" -> {
                    navController.navigate(NavItem.Tab.InCar.Media.route) { }
                }
                "more-screen" -> {
                    navController.navigate(NavItem.Tab.InCar.More.route) { }
                }
                "screen-timeout-list" -> {
                    viewModel.inCar.saveScreenTimeout(
                        disabled = (item as PreferenceItem.Switch).checked,
                        disableCharging = viewModel.inCar.isDisableScreenTimeoutCharging,
                    )
                }
                else -> onPreferenceClick(item, viewModel.inCar)
            }
        },
        placeholder = { item, paddingValues ->
            when (item.key) {
                "notif-shortcuts" -> {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NotificationShortcuts(viewModel)
                    }
                }
                "screen-timeout-list" -> {
                    ScreenTimeoutContent(viewModel.inCar)
                }
                else -> {}
            }
        }
    )
}

fun onPreferenceClick(
    item: PreferenceItem,
    inCar: InCarInterface
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
        is PreferenceItem.Text -> { }
        is PreferenceItem.Placeholder -> { }
        is PreferenceItem.Pick -> { }
    }
}

@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme {
        BackgroundSurface {
            InCarMainScreen(viewModel = viewModel())
        }
    }
}
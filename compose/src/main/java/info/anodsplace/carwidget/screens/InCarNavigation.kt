package info.anodsplace.carwidget.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesScreen
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesViewModel
import info.anodsplace.carwidget.screens.incar.MediaScreen
import info.anodsplace.carwidget.screens.incar.MoreScreen

fun NavGraphBuilder.inCarNavigation(inCar: InCarInterface,
                                    navController: NavHostController,
                                    innerPadding: PaddingValues) {
    navigation(startDestination = NavItem.InCar.Main.route, route = NavItem.InCar.route) {
        composable(NavItem.InCar.Main.route) { InCarMainScreen(inCar, navController = navController, modifier = Modifier.padding(innerPadding)) }
        composable(NavItem.InCar.Bluetooth.route) {
            val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModel()
            BluetoothDevicesScreen(viewModel = bluetoothDevicesViewModel, modifier = Modifier.padding(innerPadding))
        }
        composable(NavItem.InCar.Media.route) {
            MediaScreen(inCar = inCar, modifier = Modifier.padding(innerPadding))
        }
        composable(NavItem.InCar.More.route) {
            MoreScreen(inCar = inCar, modifier = Modifier.padding(innerPadding))
        }
    }
}
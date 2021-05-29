package info.anodsplace.carwidget.screens

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.screens.incar.*

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    onOpenWidgetConfig: (appWidgetId: Int) -> Unit
) {
    val navController = rememberNavController()
    val items: List<NavItem.TabItem> = listOf(NavItem.Widgets, NavItem.InCar, NavItem.Info)

    Scaffold(
        topBar = {
            TopAppBar {
                Text(text = stringResource(id = R.string.app_name))
            }
        },
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEachIndexed { _, item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(id = item.resourceId)) },
                        selected = currentRoute?.startsWith(item.route) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                val start = item.parent?.route ?: navController.graph.startDestinationRoute!!
                                popUpTo(start) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = NavItem.Widgets.route) {
            composable(NavItem.Widgets.route) {
                val widgetsListViewModel: WidgetsListViewModel = viewModel()
                val widgetList by widgetsListViewModel.loadList().collectAsState(initial = emptyList())
                WidgetsScreen(widgetList) { appWidgetId -> onOpenWidgetConfig(appWidgetId) }
            }
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
                    MoreScreen()
                }
            }
            composable(NavItem.Info.route) {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.initScreenState(appWidgetId = appWidgetId).collectAsState()
                AboutScreen(aboutScreenState, aboutViewModel.uiAction, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), onOpenWidgetConfig = {})
        }
    }
}

@Preview("Main Screen Dark")
@Composable
fun PreviewPreferencesScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), onOpenWidgetConfig = {})
        }
    }
}
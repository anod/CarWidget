package info.anodsplace.carwidget.screens.main

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navigation
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.ScreenLoadState
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.about.AboutScreen
import info.anodsplace.carwidget.screens.about.AboutViewModel
import info.anodsplace.carwidget.screens.incar.*
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinScreen

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    onOpenWidgetConfig: (appWidgetId: Int) -> Unit
) {
    val navController = rememberNavController()
    val startDestination = NavItem.startDestination(appWidgetId)
    val startRoute = NavItem.startRoute(appWidgetId)
    val isWidget = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
    val items: List<NavItem.TabItem> = listOf(
        if (isWidget) NavItem.CurrentWidget else NavItem.Widgets,
        NavItem.InCar,
        NavItem.Info
    )

    Scaffold(
        backgroundColor = if (isWidget) Color.Transparent else MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
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
                    val route = item.route.replace("{appWidgetId}", "$appWidgetId")
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(id = item.resourceId)) },
                        selected = currentRoute?.startsWith(item.route) == true,
                        onClick = {
                            navController.navigate(route) {
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
        val modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(innerPadding)
        NavHost(navController, startDestination = startDestination, route = startRoute) {
            composable(NavItem.Widgets.route) {
                val widgetsListViewModel: WidgetsListViewModel = viewModel()
                val widgetsState by widgetsListViewModel.loadScreen().collectAsState(initial = ScreenLoadState.Loading)
                if (widgetsState is ScreenLoadState.Ready<WidgetListScreenState>) {
                    WidgetsListScreen(
                        (widgetsState as ScreenLoadState.Ready<WidgetListScreenState>).value,
                        onClick = { appWidgetId -> onOpenWidgetConfig(appWidgetId) },
                        modifier = modifier
                    )
                }
            }
            composable(
                NavItem.CurrentWidget.route,
                arguments = listOf(navArgument("appWidgetId") { type = NavType.IntType })
            ) { backStackEntry ->
                val skinViewModel: SkinPreviewViewModel = viewModel()
                skinViewModel.appWidgetId = backStackEntry.arguments!!.getInt("appWidgetId")
                WidgetSkinScreen(skinList = skinViewModel.skinList, viewModel = skinViewModel, modifier = Modifier.padding(innerPadding))
            }
            navigation(startDestination = NavItem.InCar.Main.route, route = NavItem.InCar.route) {
                composable(NavItem.InCar.Main.route) { InCarMainScreen(inCar, navController = navController, modifier = modifier) }
                composable(NavItem.InCar.Bluetooth.route) {
                    val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModel()
                    BluetoothDevicesScreen(viewModel = bluetoothDevicesViewModel, modifier = modifier)
                }
                composable(NavItem.InCar.Media.route) {
                    MediaScreen(inCar = inCar, modifier = modifier)
                }
                composable(NavItem.InCar.More.route) {
                    MoreScreen(inCar = inCar, modifier = modifier)
                }
            }
            composable(NavItem.Info.route) {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.initScreenState(appWidgetId = appWidgetId).collectAsState()
                AboutScreen(
                    aboutScreenState,
                    aboutViewModel.uiAction,
                    modifier = modifier
                )
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
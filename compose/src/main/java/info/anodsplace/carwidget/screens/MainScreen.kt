package info.anodsplace.carwidget.screens

import android.appwidget.AppWidgetManager
import androidx.annotation.StringRes
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface

sealed class TabItem(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Widgets : TabItem("widgets", R.string.widgets, Icons.Filled.Widgets)
    object InCar : TabItem("incar", R.string.pref_incar_mode_title, Icons.Filled.DirectionsCar)
    object Info : TabItem("info", R.string.info, Icons.Outlined.Info)
}

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
) {
    val navController = rememberNavController()
    val items = listOf(TabItem.Widgets, TabItem.InCar, TabItem.Info)

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
                        icon = { Icon(item.icon, contentDescription = null ) },
                        label = { Text(stringResource(id = item.resourceId)) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationRoute!!) {
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
    ) {
        NavHost(navController, startDestination = TabItem.Widgets.route) {
            composable(TabItem.Widgets.route) { Text("Widgets") }
            composable(TabItem.InCar.route) { InCarScreen(inCar) }
            composable(TabItem.Info.route) {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.screenState.collectAsState()
                if (aboutScreenState == null) {
                    aboutViewModel.init(appWidgetId = appWidgetId)
                } else {
                    AboutScreen(aboutScreenState!!)
                }
            }
        }
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp())
        }
    }
}

@Preview("Main Screen Dark")
@Composable
fun PreviewPreferencesScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp())
        }
    }
}
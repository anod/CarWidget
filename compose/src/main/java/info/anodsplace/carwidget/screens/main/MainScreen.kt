package info.anodsplace.carwidget.screens.main

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.navigation
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.about.AboutScreen
import info.anodsplace.carwidget.screens.about.AboutViewModel
import info.anodsplace.carwidget.screens.incar.*
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetSkinScreen
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.ScreenLoadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    action: MutableSharedFlow<UiAction>
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val startDestination = NavItem.startDestination(appWidgetId)
    val startRoute = NavItem.startRoute(appWidgetId)
    val isWidget = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
    val items: List<NavItem.TabItem> = listOf(
        if (isWidget) NavItem.CurrentWidget else NavItem.Widgets,
        NavItem.InCar,
        NavItem.Info
    )
    var currentSkinValue by remember { mutableStateOf(WidgetInterface.SKIN_YOU) }

    Scaffold(
        backgroundColor = if (isWidget) Color.Transparent else MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    if (isWidget) {
                        AppBarMenu(
                            showColor = currentSkinValue == WidgetInterface.SKIN_WINDOWS7,
                            appWidgetId = appWidgetId,
                            currentSkinValue = currentSkinValue,
                            action = action
                        )
                    }
                }
            )
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
            composable(route = NavItem.Widgets.route) {
                val widgetsListViewModel: WidgetsListViewModel = viewModel()
                val widgetsState by widgetsListViewModel.loadScreen().collectAsState(initial = ScreenLoadState.Loading)
                if (widgetsState is ScreenLoadState.Ready<WidgetListScreenState>) {
                    WidgetsListScreen(
                        screen = (widgetsState as ScreenLoadState.Ready<WidgetListScreenState>).value,
                        onClick = { appWidgetId -> scope.launch { action.emit(UiAction.OpenWidgetConfig(appWidgetId)) } },
                        modifier = modifier
                    )
                }
            }
            composable(route = NavItem.CurrentWidget.route) {
                val appContext = LocalContext.current.applicationContext
                val skinViewModel: SkinPreviewViewModel = viewModel(factory = SkinPreviewViewModel.Factory(appContext, appWidgetId))
                val currentSkin by skinViewModel.currentSkin.collectAsState(initial = skinViewModel.skinList.current)
                currentSkinValue = currentSkin.value
                WidgetSkinScreen(skinList = skinViewModel.skinList, viewModel = skinViewModel, modifier = Modifier.padding(innerPadding))

                val widgetAction by action.collectAsState(initial = UiAction.None)
                if (widgetAction != UiAction.None) {
                    AppBarWidgetAction(widgetAction, action, skinViewModel.widgetSettings)
                }
            }
            navigation(startDestination = NavItem.InCar.Main.route, route = NavItem.InCar.route) {
                composable(route = NavItem.InCar.Main.route) {
                    val inCarViewModel: InCarViewModel = viewModel()
                    InCarMainScreen(inCarViewModel, navController = navController, modifier = modifier)
                }
                composable(route = NavItem.InCar.Bluetooth.route) {
                    val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModel()
                    BluetoothDevicesScreen(viewModel = bluetoothDevicesViewModel, modifier = modifier)
                }
                composable(route = NavItem.InCar.Media.route) {
                    MediaScreen(inCar = inCar, modifier = modifier)
                }
                composable(route = NavItem.InCar.More.route) {
                    MoreScreen(inCar = inCar, modifier = modifier)
                }
            }
            composable(route = NavItem.Info.route) {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.initScreenState(appWidgetId = appWidgetId).collectAsState()
                AboutScreen(
                    screenState = aboutScreenState,
                    action = aboutViewModel.uiAction,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun AppBarWidgetAction(current: UiAction, action: MutableSharedFlow<UiAction>, widgetSettings: WidgetInterface) {
    when (current) {
        is UiAction.ApplyWidget -> { }
        is UiAction.IntentEditAction -> { }
        UiAction.None -> { }
        UiAction.OnBackNav -> { }
        is UiAction.OpenWidgetConfig -> { }
        UiAction.ChooseBackgroundColor -> WidgetActionDialog(current, action, widgetSettings)
        UiAction.ChooseIconsScale -> WidgetActionDialog(current, action, widgetSettings)
        UiAction.ChooseIconsTheme -> WidgetActionDialog(current, action, widgetSettings)
        UiAction.ChooseShortcutsNumber -> WidgetActionDialog(current, action, widgetSettings)
        UiAction.ChooseTileColor -> WidgetActionDialog(current, action, widgetSettings)
        UiAction.ShowMoreSettings -> WidgetActionDialog(current, action, widgetSettings)
        is UiAction.SwitchIconsMono -> WidgetActionDialog(current, action, widgetSettings)
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), action = MutableSharedFlow())
        }
    }
}

@Preview("Main Screen Dark")
@Composable
fun PreviewPreferencesScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), action = MutableSharedFlow())
        }
    }
}
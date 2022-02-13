package info.anodsplace.carwidget.screens.main

import android.app.Application
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navigation
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings.Companion.BUTTON_COLOR
import info.anodsplace.carwidget.content.preferences.WidgetSettings.Companion.PALETTE_BG
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.WidgetActions
import info.anodsplace.carwidget.screens.about.AboutScreen
import info.anodsplace.carwidget.screens.about.AboutViewModel
import info.anodsplace.carwidget.screens.incar.*
import info.anodsplace.carwidget.screens.shortcuts.ShortcutEditScreen
import info.anodsplace.carwidget.screens.shortcuts.ShortcutEditViewModel
import info.anodsplace.carwidget.screens.shortcuts.ShortcutPickerScreen
import info.anodsplace.carwidget.screens.shortcuts.ShortcutPickerViewModel
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetActionDialog
import info.anodsplace.carwidget.screens.widget.WidgetLookMoreScreen
import info.anodsplace.carwidget.screens.widget.WidgetSkinScreen
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.ScreenLoadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun rememberTileColor(currentSkinValue: String, prefs: WidgetInterface): AppBarTileColor {
    val color by prefs.observe<Int>(BUTTON_COLOR).collectAsState(initial = prefs.tileColor)
    val palette by prefs.observe<Boolean>(PALETTE_BG).collectAsState(initial = prefs.paletteBackground)
    return remember(currentSkinValue, color, palette) {
        if (currentSkinValue == WidgetInterface.SKIN_WINDOWS7) {
            if (palette) {
                AppBarTileColor.Icon
            }
            AppBarTileColor.Value(color = Color(color))
        }
        AppBarTileColor.Hidden
    }
}

@Composable
fun MainScreen(
    inCar: InCarInterface,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    action: MutableSharedFlow<UiAction>
) {
    val navController = rememberNavController()
    val isWidget = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
    val items: List<NavItem.TabItem> = listOf(
        if (isWidget) NavItem.CurrentWidget else NavItem.Widgets,
        NavItem.InCar,
        NavItem.About
    )
    val currentSkin = remember { mutableStateOf(WidgetInterface.SKIN_YOU) }
    val widgetSettings: MutableState<WidgetInterface> = remember { mutableStateOf(WidgetInterface.NoOp()) }

    Scaffold(
        backgroundColor = if (isWidget) Color.Transparent else MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    if (isWidget) {
                        AppBarMenu(
                            tileColor = rememberTileColor(currentSkin.value, widgetSettings.value),
                            appWidgetId = appWidgetId,
                            currentSkinValue = currentSkin.value,
                            action = action,
                            navController = navController
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
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(id = item.resourceId)) },
                        selected = currentRoute?.startsWith(item.route) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = false
                            }
                        }
                    )
                }
            }
        }, content = { innerPadding ->
            NavHost(
                navController = navController,
                action = action,
                appWidgetId = appWidgetId,
                inCar = inCar,
                innerPadding = innerPadding,
                currentSkin = currentSkin,
                widgetSettings = widgetSettings
            )
        }
    )
}

@Composable
fun NavHost(
    navController: NavHostController,
    action: MutableSharedFlow<UiAction>,
    appWidgetId: Int,
    inCar: InCarInterface,
    innerPadding: PaddingValues,
    currentSkin: MutableState<String>,
    widgetSettings: MutableState<WidgetInterface>,
    startDestination: String = NavItem.startDestination(appWidgetId),
    startRoute: String? = NavItem.startRoute(appWidgetId),
    currentWidgetStartDestination: String = NavItem.CurrentWidget.Skin.route
) {
    val scope = rememberCoroutineScope()

    val modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(innerPadding)

    NavHost(navController, startDestination = startDestination, route = startRoute) {
        composable(route = NavItem.Widgets.route) {
            val widgetsListViewModel: WidgetsListViewModel = viewModel()
            val widgetsState by widgetsListViewModel.loadScreen().collectAsState(initial = ScreenLoadState.Loading)
            AppLog.d(widgetsState.toString())
            if (widgetsState is ScreenLoadState.Ready<WidgetListScreenState>) {
                WidgetsListScreen(
                    screen = (widgetsState as ScreenLoadState.Ready<WidgetListScreenState>).value,
                    onClick = { appWidgetId -> scope.launch { action.emit(UiAction.OpenWidgetConfig(appWidgetId)) } },
                    modifier = modifier
                )
            }
        }
        composable(route = NavItem.About.route) {
            val appContext = LocalContext.current.applicationContext
            val aboutViewModel: AboutViewModel = viewModel(factory = AboutViewModel.Factory(appContext, appWidgetId))
            val aboutScreenState by aboutViewModel.screenState.collectAsState(initial = null)
            if (aboutScreenState != null) {
                AboutScreen(
                        screenState = aboutScreenState!!,
                        action = aboutViewModel.uiAction,
                        modifier = modifier
                )
            }
        }
        navigation(route = NavItem.CurrentWidget.route, startDestination = currentWidgetStartDestination) {
            composable(route = NavItem.CurrentWidget.Skin.route) {
                val appContext = LocalContext.current.applicationContext
                val skinViewModel: SkinPreviewViewModel = viewModel(factory = SkinPreviewViewModel.Factory(appContext, appWidgetId))
                val currentSkinValue by skinViewModel.currentSkin.collectAsState(initial = skinViewModel.skinList.current)
                currentSkin.value = currentSkinValue.value
                widgetSettings.value = skinViewModel.widgetSettings
                WidgetSkinScreen(
                    skinList = skinViewModel.skinList,
                    viewModel = skinViewModel,
                    modifier = Modifier.padding(innerPadding)
                )

                val widgetAction by action.collectAsState(initial = UiAction.None)
                if (widgetAction != UiAction.None) {
                    AppBarWidgetAction(modifier, widgetAction, action, skinViewModel.widgetSettings)
                }
            }
            composable(
                route = NavItem.CurrentWidget.EditShortcut.route,
                arguments = NavItem.CurrentWidget.EditShortcut.arguments,
                deepLinks = NavItem.CurrentWidget.EditShortcut.deepLinks
            ) {
                EditShortcut(
                        appWidgetId = appWidgetId,
                        args = NavItem.CurrentWidget.EditShortcut.Args(it.arguments),
                        action = action
                )
            }
            composable(route = NavItem.CurrentWidget.MoreSettings.route) {
                val appContext = LocalContext.current.applicationContext
                val skinViewModel: SkinPreviewViewModel =
                    viewModel(factory = SkinPreviewViewModel.Factory(appContext, appWidgetId))
                WidgetLookMoreScreen(
                    settings = skinViewModel.widgetSettings,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        navigation(route = NavItem.InCar.route, startDestination = NavItem.InCar.Main.route) {
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
    }
}

@Composable
fun EditShortcut(appWidgetId: Int, args: NavItem.CurrentWidget.EditShortcut.Args, action: MutableSharedFlow<UiAction>,) {
    val appContext = LocalContext.current.applicationContext as Application
    val scope = rememberCoroutineScope()

    if (args.shortcutId > 0) {
        val viewModel: ShortcutEditViewModel = viewModel(
                factory = ShortcutEditViewModel.Factory(
                        args.position,
                        args.shortcutId,
                        appWidgetId,
                        appContext
                )
        )
        ShortcutEditScreen(viewModel) {
            scope.launch { action.emit(UiAction.OnBackNav) }
        }
    } else {
        val viewModel: ShortcutPickerViewModel = viewModel(
                factory = ShortcutPickerViewModel.Factory(
                        args.position,
                        appWidgetId,
                        appContext
                )
        )
        ShortcutPickerScreen(viewModel) {
            scope.launch { action.emit(UiAction.OnBackNav) }
        }
    }
}

@Composable
fun AppBarWidgetAction(
    modifier: Modifier,
    current: UiAction,
    action: MutableSharedFlow<UiAction>,
    widgetSettings: WidgetInterface
) {
    when (current) {
        WidgetActions.ChooseBackgroundColor -> WidgetActionDialog(modifier, current, action, widgetSettings)
        WidgetActions.ChooseIconsScale -> WidgetActionDialog(modifier, current, action, widgetSettings)
        WidgetActions.ChooseIconsTheme -> WidgetActionDialog(modifier, current, action, widgetSettings)
        WidgetActions.ChooseShortcutsNumber -> WidgetActionDialog(modifier, current, action, widgetSettings)
        WidgetActions.ChooseTileColor -> WidgetActionDialog(modifier, current, action, widgetSettings)
        is WidgetActions.SwitchIconsMono -> WidgetActionDialog(modifier, current, action, widgetSettings)
        else -> {}
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme() {
        BackgroundSurface {
            MainScreen(InCarInterface.NoOp(), action = MutableSharedFlow())
        }
    }
}
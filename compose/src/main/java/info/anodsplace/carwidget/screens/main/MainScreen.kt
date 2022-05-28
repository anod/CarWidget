package info.anodsplace.carwidget.screens.main

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.WidgetActions
import info.anodsplace.carwidget.screens.about.AboutScreen
import info.anodsplace.carwidget.screens.about.AboutViewModel
import info.anodsplace.carwidget.screens.incar.*
import info.anodsplace.carwidget.screens.shortcuts.EditShortcut
import info.anodsplace.carwidget.screens.widget.SkinPreviewViewModel
import info.anodsplace.carwidget.screens.widget.WidgetActionDialog
import info.anodsplace.carwidget.screens.widget.WidgetLookMoreScreen
import info.anodsplace.carwidget.screens.widget.WidgetSkinScreen
import info.anodsplace.carwidget.screens.wizard.WizardScreen
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.RequestPermissionsScreen
import info.anodsplace.compose.ScreenLoadState
import info.anodsplace.compose.findActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun MainScreen(
    mainViewModel: MainViewModel
) {
    when (mainViewModel.topDestination.value) {
        NavItem.Wizard -> {
            WizardScreen()
        }
        NavItem.PermissionsRequest -> {
            val context = LocalContext.current
            val permissionsViewModel: PermissionsViewModel = viewModel(factory = PermissionsViewModel.Factory(
                requiredPermissions = mainViewModel.requiredPermissions,
                activity = context.findActivity()
            ))
            RequestPermissionsScreen(
                input = permissionsViewModel.missingPermissions,
                screenDescription = permissionsViewModel.screenDescription) {
                if (permissionsViewModel.updatePermissions(context.findActivity())) {
                    mainViewModel.onPermissionsAcquired()
                }
            }
        }
        else -> {
            Tabs(mainViewModel = mainViewModel)
        }
    }

    if (mainViewModel.showProDialog.value) {
        AlertDialog(
            onDismissRequest = { mainViewModel.showProDialog.value = false },
            buttons = { },
            title = { Text(text = stringResource(id = R.string.dialog_donate_title_install)) },
            text = { Text(text = stringResource(id = R.string.dialog_donate_message_installed)) },
        )
    }
}

@Composable
fun Tabs(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentSkin = remember { mutableStateOf(WidgetInterface.SKIN_YOU) }

    Scaffold(
        backgroundColor = if (mainViewModel.isWidget) Color.Transparent else MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    if (mainViewModel.isWidget) {
                        AppBarMenu(
                            tileColor = rememberTileColor(currentSkin.value, mainViewModel.widgetSettings),
                            appWidgetId = +mainViewModel.appWidgetIdScope,
                            currentSkinValue = currentSkin.value,
                            action = mainViewModel.action,
                            navController = navController
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomTabs(mainViewModel.tabs, navController)
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                action = mainViewModel.action,
                appWidgetIdScope = mainViewModel.appWidgetIdScope,
                inCar = mainViewModel.inCarSettings,
                innerPadding = innerPadding,
                currentSkin = currentSkin,
                startDestination = mainViewModel.topDestination.value.route,
                startRoute = mainViewModel.startRoute
            )
        }
    )
}

@Composable
fun BottomTabs(items: List<NavItem.Tab>, navController: NavHostController) {
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
}

@Composable
fun NavHost(
    navController: NavHostController,
    action: MutableSharedFlow<UiAction>,
    appWidgetIdScope: AppWidgetIdScope? = null,
    inCar: InCarInterface,
    innerPadding: PaddingValues,
    currentSkin: MutableState<String>,
    startDestination: String,
    startRoute: String?,
    currentWidgetStartDestination: String = NavItem.Tab.CurrentWidget.Skin.route
) {
    val scope = rememberCoroutineScope()

    val modifier = Modifier
        .background(MaterialTheme.colors.background)
        .padding(innerPadding)

    NavHost(navController, startDestination = startDestination, route = startRoute) {
        composable(route = NavItem.Tab.Widgets.route) {
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
        composable(route = NavItem.Tab.About.route) {
            val appContext = LocalContext.current.applicationContext
            val aboutViewModel: AboutViewModel = viewModel(factory = AboutViewModel.Factory(appContext, appWidgetIdScope))
            val aboutScreenState by aboutViewModel.screenState.collectAsState(initial = null)
            if (aboutScreenState != null) {
                AboutScreen(
                        screenState = aboutScreenState!!,
                        action = aboutViewModel.uiAction,
                        modifier = modifier
                )
            }
        }
        navigation(route = NavItem.Tab.CurrentWidget.route, startDestination = currentWidgetStartDestination) {
            composable(route = NavItem.Tab.CurrentWidget.Skin.route) {
                val skinViewModel: SkinPreviewViewModel = viewModel(factory = SkinPreviewViewModel.Factory(LocalContext.current, appWidgetIdScope!!))
                val currentSkinValue by skinViewModel.currentSkin.collectAsState(initial = skinViewModel.skinList.current)
                currentSkin.value = currentSkinValue.value
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
                route = NavItem.Tab.CurrentWidget.EditShortcut.route,
                arguments = NavItem.Tab.CurrentWidget.EditShortcut.arguments,
                deepLinks = NavItem.Tab.CurrentWidget.EditShortcut.deepLinks
            ) {
                EditShortcut(
                        appWidgetIdScope = appWidgetIdScope!!,
                        args = NavItem.Tab.CurrentWidget.EditShortcut.Args(it.arguments),
                        action = action
                )
            }
            composable(route = NavItem.Tab.CurrentWidget.MoreSettings.route) {
                val appContext = LocalContext.current.applicationContext
                val skinViewModel: SkinPreviewViewModel =
                    viewModel(factory = SkinPreviewViewModel.Factory(appContext, appWidgetIdScope!!))
                WidgetLookMoreScreen(
                    settings = skinViewModel.widgetSettings,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        navigation(route = NavItem.Tab.InCar.route, startDestination = NavItem.Tab.InCar.Main.route) {
            composable(route = NavItem.Tab.InCar.Main.route) {
                val inCarViewModel: InCarViewModel = viewModel()
                InCarMainScreen(inCarViewModel, navController = navController, modifier = modifier)
            }
            composable(route = NavItem.Tab.InCar.Bluetooth.route) {
                val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModel(factory =
                    BluetoothDevicesViewModel.Factory(
                        application = getKoin().get(),
                        bluetoothManager = getKoin().get(),
                        settings = getKoin().get(),
                    )
                )
                BluetoothDevicesScreen(viewModel = bluetoothDevicesViewModel, modifier = modifier)
            }
            composable(route = NavItem.Tab.InCar.Media.route) {
                MediaScreen(inCar = inCar, modifier = modifier)
            }
            composable(route = NavItem.Tab.InCar.More.route) {
                MoreScreen(inCar = inCar, modifier = modifier)
            }
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
    CarWidgetTheme {
        BackgroundSurface {
            MainScreen(MainViewModel(
                emptyList(),
                null,
                LocalContext.current.applicationContext as Application
            ))
        }
    }
}
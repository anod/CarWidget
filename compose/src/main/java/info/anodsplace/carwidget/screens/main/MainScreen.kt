package info.anodsplace.carwidget.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navigation
import coil.ImageLoader
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.about.AboutScreen
import info.anodsplace.carwidget.screens.about.AboutViewModel
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesScreen
import info.anodsplace.carwidget.screens.incar.BluetoothDevicesViewModel
import info.anodsplace.carwidget.screens.incar.InCarMainScreen
import info.anodsplace.carwidget.screens.incar.InCarViewModel
import info.anodsplace.carwidget.screens.shortcuts.EditShortcut
import info.anodsplace.carwidget.screens.widget.*
import info.anodsplace.carwidget.screens.wizard.WizardScreen
import info.anodsplace.compose.RequestPermissionsScreen
import info.anodsplace.compose.findActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun MainScreen(
    screenState: MainViewState,
    onEvent: (event: MainViewEvent) -> Unit = { },
    onViewAction: (action: MainViewAction) -> Unit = { },
    viewActions: Flow<MainViewAction> = emptyFlow(),
    appWidgetIdScope: AppWidgetIdScope? = null,
    imageLoader: ImageLoader
) {
    when (screenState.topDestination) {
        NavItem.Wizard -> {
            WizardScreen(screenState, onEvent = onEvent)
        }
        NavItem.PermissionsRequest -> {
            val context = LocalContext.current
            val permissionsViewModel: PermissionsViewModel = viewModel(
                factory = PermissionsViewModel.Factory(
                    requiredPermissions = screenState.requiredPermissions,
                    activity = context.findActivity()
                )
            )
            val viewState by permissionsViewModel.viewStates.collectAsState(initial = permissionsViewModel.viewState)
            RequestPermissionsScreen(
                input = viewState.missingPermissions,
                screenDescription = viewState.screenDescription
            ) {
                if (permissionsViewModel.updatePermissions(context.findActivity())) {
                    onEvent(MainViewEvent.PermissionAcquired)
                }
            }
        }
        else -> {
            Tabs(
                screenState = screenState,
                onEvent = onEvent,
                viewActions = viewActions,
                onViewAction = onViewAction,
                appWidgetIdScope = appWidgetIdScope,
                imageLoader = imageLoader
            )
        }
    }

    if (screenState.showProDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(MainViewEvent.HideProDialog) },
            title = { Text(text = stringResource(id = R.string.dialog_donate_title_install)) },
            text = { Text(text = stringResource(id = R.string.dialog_donate_message_installed)) },
            confirmButton = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tabs(
    screenState: MainViewState,
    onEvent: (event: MainViewEvent) -> Unit,
    viewActions: Flow<MainViewAction> = emptyFlow(),
    onViewAction: (action: MainViewAction) -> Unit = { },
    appWidgetIdScope: AppWidgetIdScope? = null,
    imageLoader: ImageLoader
) {
    val navController = rememberNavController()
    val currentSkin = remember { mutableStateOf(WidgetInterface.SKIN_YOU) }

    Scaffold(
        containerColor = if (screenState.isWidget) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    if (screenState.isWidget) {
                        AppBarActions(
                            isIconsMono = screenState.widgetSettings.isIconsMono,
                            tileColor = rememberTileColor(currentSkin.value, screenState.widgetSettings),
                            appWidgetId = screenState.appWidgetId,
                            currentSkinValue = currentSkin.value,
                            onEvent = onEvent,
                            navController = navController
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomTabs(screenState.tabs, navController)
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                onEvent = onEvent,
                appWidgetIdScope = appWidgetIdScope,
                innerPadding = innerPadding,
                currentSkin = currentSkin,
                startDestination = screenState.topDestination.route,
                startRoute = screenState.startRoute,
                imageLoader = imageLoader
            )
        }
    )

    LaunchedEffect(key1 = true) {
        viewActions.collect { action ->
            when (action) {
                is MainViewAction.ShowDialog -> {
                    navController.navigate(action.route)
                }
                else -> onViewAction(action)
            }
        }
    }
}

@Composable
fun BottomTabs(items: List<NavItem.Tab>, navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEachIndexed { _, item ->
            NavigationBarItem(
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
    onEvent: (MainViewEvent) -> Unit,
    appWidgetIdScope: AppWidgetIdScope? = null,
    innerPadding: PaddingValues,
    currentSkin: MutableState<String>,
    startDestination: String,
    startRoute: String?,
    currentWidgetStartDestination: String = NavItem.Tab.CurrentWidget.Skin.route,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current

    NavHost(navController, startDestination = startDestination, route = startRoute) {
        composable(route = NavItem.Tab.Widgets.route) {
            val widgetsListViewModel: WidgetsListViewModel = viewModel()
            val widgetsState by widgetsListViewModel.viewStates.collectAsState(initial = widgetsListViewModel.viewState)
            WidgetsListScreen(
                screen = widgetsState,
                onClick = { appWidgetId -> onEvent(MainViewEvent.OpenWidgetConfig(appWidgetId)) },
                innerPadding = innerPadding,
                imageLoader = imageLoader
            )
        }
        composable(route = NavItem.Tab.About.route) {
            val aboutViewModel: AboutViewModel = viewModel(factory = AboutViewModel.Factory(appWidgetIdScope))
            val aboutScreenState by aboutViewModel.viewStates.collectAsState(initial = aboutViewModel.viewState)
            AboutScreen(
                screenState = aboutScreenState,
                onEvent = { aboutViewModel.handleEvent(it) },
                innerPadding = innerPadding,
                imageLoader = imageLoader
            )
        }
        navigation(route = NavItem.Tab.CurrentWidget.route, startDestination = currentWidgetStartDestination) {
            composable(route = NavItem.Tab.CurrentWidget.Skin.route) {
                val skinViewModel: SkinPreviewViewModel =
                    viewModel(factory = SkinPreviewViewModel.Factory(appWidgetIdScope!!))
                val screenState by skinViewModel.viewStates.collectAsState(initial = skinViewModel.viewState)
                currentSkin.value = screenState.currentSkin.value
                WidgetSkinScreen(
                    screenState = screenState,
                    innerPadding = innerPadding,
                    skinViewFactory = skinViewModel,
                    onEvent = { skinViewModel.handleEvent(it) }
                )
            }
            dialog(
                route = NavItem.Tab.CurrentWidget.Skin.Dialog.route,
                arguments = NavItem.Tab.CurrentWidget.Skin.Dialog.arguments,
                dialogProperties = DialogProperties()
            ) { entry ->
                val args = NavItem.Tab.CurrentWidget.Skin.Dialog.Args(entry.arguments)
                val skinViewModel: SkinPreviewViewModel =
                    viewModel(factory = SkinPreviewViewModel.Factory(appWidgetIdScope!!))
                val screenState by skinViewModel.viewStates.collectAsState(initial = skinViewModel.viewState)
                WidgetActionDialogContent(
                    args.dialogType,
                    onEvent = { skinViewModel.handleEvent(it) },
                    dismiss = { navController.popBackStack() },
                    widgetSettings = screenState.widgetSettings
                )
            }
            composable(
                route = NavItem.Tab.CurrentWidget.EditShortcut.route,
                arguments = NavItem.Tab.CurrentWidget.EditShortcut.arguments,
                deepLinks = NavItem.Tab.CurrentWidget.EditShortcut.deepLinks
            ) {
                EditShortcut(
                    appWidgetIdScope = appWidgetIdScope!!,
                    args = NavItem.Tab.CurrentWidget.EditShortcut.Args(it.arguments),
                    onDismissRequest = { onEvent(MainViewEvent.OnBackNav) }
                )
            }
            composable(route = NavItem.Tab.CurrentWidget.MoreSettings.route) {
                val lookMoreViewModel: WidgetLookMoreViewModel = viewModel(factory = WidgetLookMoreViewModel.Factory(appWidgetIdScope!!))
                val screenState by lookMoreViewModel.viewStates.collectAsState(initial = lookMoreViewModel.viewState)
                WidgetLookMoreScreen(
                    screenState = screenState,
                    onEvent = { lookMoreViewModel.handleEvent(it) },
                    innerPadding = innerPadding
                )
            }
        }
        navigation(route = NavItem.Tab.InCar.route, startDestination = NavItem.Tab.InCar.Main.route) {
            composable(route = NavItem.Tab.InCar.Main.route) {
                val inCarViewModel: InCarViewModel = viewModel()
                val screenState by inCarViewModel.viewStates.collectAsState(initial = inCarViewModel.viewState)
                InCarMainScreen(
                    screenState = screenState,
                    onEvent = { inCarViewModel.handleEvent(it) },
                    innerPadding = innerPadding,
                    appsLoader = inCarViewModel.appsLoader,
                    imageLoader = imageLoader
                )
                LaunchedEffect(true) {
                    inCarViewModel.viewActions.collect {
                        inCarViewModel.handleAction(it, navController, activityContext = context)
                    }
                }
            }
            composable(route = NavItem.Tab.InCar.Bluetooth.route) {
                val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModel(
                    factory =
                    BluetoothDevicesViewModel.Factory(
                        bluetoothManager = getKoin().get(),
                        settings = getKoin().get(),
                    )
                )
                val screenState by bluetoothDevicesViewModel.viewStates.collectAsState(initial = bluetoothDevicesViewModel.viewState)
                BluetoothDevicesScreen(
                    screenState = screenState,
                    onEvent = { bluetoothDevicesViewModel.handleEvent(it) },
                    innerPadding = innerPadding
                )
            }
        }
    }
}

@Preview("Main Screen Light")
@Composable
fun PreviewPreferencesScreenLight() {
    CarWidgetTheme {
        Surface {
            MainScreen(
                screenState = MainViewState(),
                imageLoader = ImageLoader.Builder(LocalContext.current).build()
            )
        }
    }
}
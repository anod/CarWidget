package info.anodsplace.carwidget.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import coil.ImageLoader
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.CheckIcon
import info.anodsplace.carwidget.RouteNameSpace
import info.anodsplace.carwidget.SceneNavKey
import info.anodsplace.carwidget.TabNavKey
import info.anodsplace.carwidget.about.AboutScreen
import info.anodsplace.carwidget.about.AboutViewModel
import info.anodsplace.carwidget.appwidget.RealSkinPreviewViewModel
import info.anodsplace.carwidget.appwidget.SkinList
import info.anodsplace.carwidget.appwidget.SkinPreviewViewModel
import info.anodsplace.carwidget.appwidget.WidgetCustomizeScreen
import info.anodsplace.carwidget.appwidget.WidgetCustomizeViewModel
import info.anodsplace.carwidget.appwidget.WidgetSkinScreen
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.incar.BluetoothDevicesScreen
import info.anodsplace.carwidget.incar.BluetoothDevicesViewModel
import info.anodsplace.carwidget.incar.InCarMainScreen
import info.anodsplace.carwidget.incar.InCarViewModel
import info.anodsplace.carwidget.permissions.PermissionsScreen
import info.anodsplace.carwidget.shortcut.EditShortcut
import info.anodsplace.framework.app.findActivity
import info.anodsplace.framework.content.onScreenCommonAction
import info.anodsplace.framework.content.startActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun MainScreen(
    screenState: MainViewState,
    windowSizeClass: WindowSizeClass,
    onEvent: (event: MainViewEvent) -> Unit = { },
    onViewAction: (action: MainViewAction) -> Unit = { },
    viewActions: Flow<MainViewAction> = emptyFlow(),
    appWidgetIdScope: AppWidgetIdScope? = null,
    imageLoader: ImageLoader,
) {
    val context = LocalContext.current
    when (screenState.topDestination) {
        SceneNavKey.Wizard -> {
            WizardScreen(onEvent = onEvent)
            LaunchedEffect(key1 = true) {
                viewActions.collect { action ->
                    when (action) {
                        is MainViewAction.StartActivity -> context.startActivity(action)
                        else -> onViewAction(action)
                    }
                }
            }
        }
        SceneNavKey.PermissionsRequest -> {
            PermissionsScreen(screenState = screenState, onEvent = onEvent)
        }
        else -> {
            val navController = rememberNavController()
            val isCompact = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
            if (isCompact) {
                NavRail(
                    navController = navController,
                    screenState = screenState,
                    windowSizeClass = windowSizeClass,
                    onEvent = onEvent,
                    appWidgetIdScope = appWidgetIdScope,
                    imageLoader = imageLoader
                )
            } else {
                Tabs(
                    navController = navController,
                    screenState = screenState,
                    windowSizeClass = windowSizeClass,
                    onEvent = onEvent,
                    appWidgetIdScope = appWidgetIdScope,
                    imageLoader = imageLoader
                )
            }

            LaunchedEffect(key1 = true) {
                viewActions.collect { action ->
                    when (action) {
                        is MainViewAction.ShowDialog -> {
                            navController.navigate(action.route)
                        }
                        is MainViewAction.StartActivity -> context.startActivity(action)
                        else -> onViewAction(action)
                    }
                }
            }
        }
    }
}

@Composable
fun NavRail(
    navController: NavHostController,
    screenState: MainViewState,
    windowSizeClass: WindowSizeClass,
    onEvent: (event: MainViewEvent) -> Unit,
    appWidgetIdScope: AppWidgetIdScope?,
    imageLoader: ImageLoader
) {
    val navRailInset = WindowInsets(0.dp, 0.dp, 80.dp, 0.dp)
    Box {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = null // TODO: navBackStackEntry?.toRoute()
        NavHost(
            navController = navController,
            onEvent = onEvent,
            appWidgetIdScope = appWidgetIdScope,
            innerPadding = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                .add(WindowInsets.statusBars.only(WindowInsetsSides.Vertical))
                .add(WindowInsets.navigationBars)
                .add(navRailInset).asPaddingValues(),
            startDestination = screenState.topDestination,
            routeNS = screenState.routeNS,
            imageLoader = imageLoader,
            windowSizeClass = windowSizeClass,
        )
        NavRailMenu(
            modifier = Modifier.align(Alignment.CenterEnd),
            items = screenState.tabs,
            currentRoute = currentRoute,
            onClick = { item -> navController.navigate(item) },
            showApply = screenState.isWidget,
            onApply = { onEvent(MainViewEvent.ApplyWidget(screenState.appWidgetId, screenState.skinList.current.value)) },
            windowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Vertical)
                .add(WindowInsets.displayCutout.only(WindowInsetsSides.End))
                .add(WindowInsets.navigationBars.only(WindowInsetsSides.End)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tabs(
    navController: NavHostController,
    screenState: MainViewState,
    windowSizeClass: WindowSizeClass,
    onEvent: (event: MainViewEvent) -> Unit,
    appWidgetIdScope: AppWidgetIdScope? = null,
    imageLoader: ImageLoader
) {
    Scaffold(
        containerColor = if (screenState.isWidget) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = info.anodsplace.carwidget.content.R.string.app_name)) },
                actions = {
                    if (screenState.isWidget) {
                        IconButton(onClick = { onEvent(
                            MainViewEvent.ApplyWidget(
                                screenState.appWidgetId,
                                screenState.skinList.current.value
                            )
                        ) }) {
                            CheckIcon()
                        }
                    }
                }
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = null // TODO: navBackStackEntry?.destination?.route
            BottomTabsMenu(
                items = screenState.tabs,
                currentRoute = currentRoute,
                onClick = { item -> navController.navigate(item) }
            )
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                onEvent = onEvent,
                appWidgetIdScope = appWidgetIdScope,
                innerPadding = innerPadding,
                startDestination = screenState.topDestination,
                routeNS = screenState.routeNS,
                imageLoader = imageLoader,
                windowSizeClass = windowSizeClass
            )
        }
    )
}

@Composable
fun NavHost(
    navController: NavHostController,
    onEvent: (MainViewEvent) -> Unit,
    appWidgetIdScope: AppWidgetIdScope? = null,
    innerPadding: PaddingValues,
    startDestination: SceneNavKey,
    routeNS: RouteNameSpace,
    currentWidgetStartDestination: SceneNavKey = SceneNavKey.Skin,
    imageLoader: ImageLoader,
    windowSizeClass: WindowSizeClass,
) {
    val context = LocalContext.current

    NavHost(navController, startDestination = startDestination, route = routeNS::class) {
        composable<SceneNavKey.Widgets> {
            val widgetsListViewModel: WidgetsListViewModel = viewModel()
            val widgetsState by widgetsListViewModel.viewStates.collectAsState(initial = widgetsListViewModel.viewState)
            WidgetsListScreen(
                screen = widgetsState,
                onEvent = onEvent,
                innerPadding = innerPadding,
                imageLoader = imageLoader
            )
        }
        composable<SceneNavKey.About> {
            val aboutViewModel: AboutViewModel = viewModel(factory = AboutViewModel.Factory(appWidgetIdScope))
            val aboutScreenState by aboutViewModel.viewStates.collectAsState(initial = aboutViewModel.viewState)
            AboutScreen(
                screenState = aboutScreenState,
                onEvent = aboutViewModel::handleEvent,
                innerPadding = innerPadding,
                imageLoader = imageLoader
            )

            LaunchedEffect(key1 = true) {
                aboutViewModel.viewActions.collect {
                    aboutViewModel.handleAction(it, context.findActivity())
                }
            }

        }
        navigation<SceneNavKey.CurrentWidget>(startDestination = currentWidgetStartDestination) {
            composable<SceneNavKey.Skin> {
                val skinViewModel: RealSkinPreviewViewModel = viewModel(
                    factory = SkinPreviewViewModel.Factory(appWidgetIdScope!!),
                    key = SceneNavKey.Skin.toString())
                val screenState by skinViewModel.viewStates.collectAsState(initial = skinViewModel.viewState)
                WidgetSkinScreen(
                    screenState = screenState,
                    windowSizeClass = windowSizeClass,
                    innerPadding = innerPadding,
                    skinViewFactory = skinViewModel,
                    onMainEvent = onEvent
                )
            }
            composable<SceneNavKey.EditShortcut>(
                deepLinks = SceneNavKey.EditShortcut.deepLinks
            ) { navBackStackEntry ->
                EditShortcut(
                    appWidgetIdScope = appWidgetIdScope!!,
                    args = navBackStackEntry.toRoute(),
                    onDismissRequest = { onEvent(MainViewEvent.OnBackNav) }
                )
            }
            composable<SceneNavKey.WidgetCustomize> {
                val customizeViewModel: WidgetCustomizeViewModel = viewModel(factory = WidgetCustomizeViewModel.Factory(appWidgetIdScope!!))
                val screenState by customizeViewModel.viewStates.collectAsState(initial = customizeViewModel.viewState)
                WidgetCustomizeScreen(
                    screenState = screenState,
                    onEvent = customizeViewModel::handleEvent,
                    innerPadding = innerPadding,
                    isCompact = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact,
                    skinViewFactory = customizeViewModel,
                    imageLoader = imageLoader
                )
                val context = LocalContext.current
                LaunchedEffect(true) {
                    customizeViewModel.viewActions.collect { action ->
                        context.onScreenCommonAction(action, navigateBack = {})
                    }
                }
            }
        }
        navigation<SceneNavKey.InCar>(startDestination = SceneNavKey.Main) {
            composable<SceneNavKey.Main> {
                val inCarViewModel: InCarViewModel = viewModel(factory = InCarViewModel.Factory(context.findActivity()))
                val screenState by inCarViewModel.viewStates.collectAsState(initial = inCarViewModel.viewState)
                InCarMainScreen(
                    screenState = screenState,
                    onEvent = inCarViewModel::handleEvent,
                    innerPadding = innerPadding,
                    appsLoader = inCarViewModel.appsLoader,
                    imageLoader = imageLoader
                )
                LaunchedEffect(true) {
                    inCarViewModel.viewActions.collect {
                        inCarViewModel.handleAction(it, navController, context.findActivity())
                    }
                }
            }
            composable<SceneNavKey.Bluetooth> {
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
                    onEvent = bluetoothDevicesViewModel::handleEvent,
                    innerPadding = innerPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview("Main Screen Light")
@Composable
fun PreviewMainScreenLight() {
    CarWidgetTheme {
        Surface {
            MainScreen(
                screenState = MainViewState(skinList = SkinList(WidgetInterface.SKIN_YOU, LocalContext.current)),
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp)),
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview("Main Screen Compact")
@Composable
fun PreviewMainScreenCompact() {
    CarWidgetTheme {
        Surface {
            MainScreen(
                screenState = MainViewState(skinList = SkinList(WidgetInterface.SKIN_YOU, LocalContext.current)),
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(480.dp, 600.dp)),
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            )
        }
    }
}
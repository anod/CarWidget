package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.NavKey
import info.anodsplace.carwidget.content.di.getOrCreateAppWidgetScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.main.MainScreen
import info.anodsplace.carwidget.main.MainViewAction
import info.anodsplace.carwidget.main.MainViewModel
import info.anodsplace.carwidget.main.Navigator
import info.anodsplace.ktx.extras
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class MainComposeActivity : AppCompatActivity(), KoinComponent {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(
            appWidgetId = appWidgetId,
            activity = this@MainComposeActivity,
            permissionChecker = get(),
            appSettings = get()
        )
    }

    open fun startConfigActivity(appWidgetId: Int) { }
    open fun requestWidgetUpdate(appWidgetId: Int) { }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(appSettings.appCompatNightMode)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            LaunchedEffect(uiMode) {
                uiModeManager.nightMode = uiMode
            }

            val screenState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
            val navigationState = rememberNavigationState(
                startRoute = screenState.topDestination,
                topLevelRoutes = screenState.tabs as Set<NavKey>
            )
            val navigator = remember { Navigator(navigationState) }

            CarWidgetTheme(
                uiMode = uiMode
            ) {
                val windowSizeClass = calculateWindowSizeClass(this)
                MainScreen(
                    screenState = screenState,
                    windowSizeClass = windowSizeClass,
                    onEvent = mainViewModel::handleEvent,
                    onViewAction = ::onViewAction,
                    viewActions = mainViewModel.viewActions,
                    appWidgetIdScope = mainViewModel.appWidgetIdScope,
                    imageLoader = mainViewModel.imageLoader,
                    navigator = navigator,
                    navigationState = navigationState
                )
            }
        }
    }

    private fun onViewAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.OpenWidgetConfig -> startConfigActivity(action.appWidgetId)
            is MainViewAction.ApplyWidget -> {
                val resultValue = Intent().putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    action.appWidgetId
                )
                setResult(RESULT_OK, resultValue)
                getOrCreateAppWidgetScope(action.appWidgetId).use {
                    val prefs: WidgetInterface = it.scope.get()
                    prefs.skin = action.currentSkinValue
                    prefs.applyPending()
                }
                requestWidgetUpdate(action.appWidgetId)
                finish()
            }
            is MainViewAction.OnBackNav -> onBackPressedDispatcher.onBackPressed()
            is MainViewAction.StartActivity -> { }
        }
    }
}
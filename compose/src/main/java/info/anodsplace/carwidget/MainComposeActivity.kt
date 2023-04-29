package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
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
import androidx.core.view.WindowCompat
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.main.MainScreen
import info.anodsplace.carwidget.main.MainViewAction
import info.anodsplace.carwidget.main.MainViewModel
import info.anodsplace.framework.content.onCommonActivityAction
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
            inCarStatus = get()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LaunchedEffect(uiMode) {
                    uiModeManager.nightMode = uiMode
                }
            }
            CarWidgetTheme(
                uiMode = uiMode
            ) {
                val windowSizeClass = calculateWindowSizeClass(this)
                val screenState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
                MainScreen(
                    screenState = screenState,
                    windowSizeClass = windowSizeClass,
                    onEvent = mainViewModel::handleEvent,
                    onViewAction = ::onViewAction,
                    viewActions = mainViewModel.viewActions,
                    appWidgetIdScope = mainViewModel.appWidgetIdScope,
                    imageLoader = mainViewModel.imageLoader,
                    onActivityAction = ::onCommonActivityAction
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
                AppWidgetIdScope(action.appWidgetId).use {
                    val prefs: WidgetInterface = it.scope.get()
                    prefs.skin = action.currentSkinValue
                    prefs.applyPending()
                }
                requestWidgetUpdate(action.appWidgetId)
                finish()
            }
            is MainViewAction.OnBackNav -> onBackPressed()
            is MainViewAction.ShowDialog -> { }
            is MainViewAction.ActivityAction -> { }
        }
    }
}
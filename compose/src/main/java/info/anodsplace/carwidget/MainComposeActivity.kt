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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.main.MainScreen
import info.anodsplace.carwidget.screens.main.MainViewAction
import info.anodsplace.carwidget.screens.main.MainViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(appSettings.appCompatNightMode)
        super.onCreate(savedInstanceState)
        appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LaunchedEffect(uiMode) {
                    uiModeManager.setApplicationNightMode(uiMode)
                }
            }
            CarWidgetTheme(
                uiMode = uiMode
            ) {
                val screenState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
                MainScreen(
                    screenState = screenState,
                    viewActions = mainViewModel.viewActions,
                    onViewAction = { onViewAction(it) },
                    onEvent = { mainViewModel.handleEvent(it) },
                    appWidgetIdScope = mainViewModel.appWidgetIdScope,
                    imageLoader = mainViewModel.imageLoader
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
            MainViewAction.OnBackNav -> onBackPressed()
            is MainViewAction.ShowDialog -> { }
        }
    }
}
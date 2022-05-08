package info.anodsplace.carwidget

import android.app.Activity
import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.main.MainScreen
import info.anodsplace.compose.toColorHex
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class MainComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()
    private var appWidgetIdScope: AppWidgetIdScope? = null

    open fun startConfigActivity(appWidgetId: Int) {}
    open fun requestWidgetUpdate(appWidgetId: Int) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(appSettings.nightMode)
        }
        super.onCreate(savedInstanceState)
        val appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        appWidgetIdScope = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) AppWidgetIdScope(appWidgetId) else null

        val action = MutableSharedFlow<UiAction>()
        lifecycleScope.launchWhenResumed {
            action.collect { action ->
                when (action) {
                    is UiAction.OpenWidgetConfig -> startConfigActivity(action.appWidgetId)
                    is UiAction.ApplyWidget -> {
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, action.appWidgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                        AppWidgetIdScope(action.appWidgetId).use {
                            val prefs: WidgetInterface = it.scope.get()
                            prefs.skin = action.skinValue
                            prefs.applyPending()
                        }
                        requestWidgetUpdate(action.appWidgetId)
                        finish()
                    }
                    else -> { }
                }
            }
        }
        setContent {
            val nightMode by appSettings.nightModeChange.collectAsState(initial = appSettings.nightMode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LaunchedEffect(nightMode) {
                    uiModeManager.setApplicationNightMode(nightMode)
                }
            }
            CarWidgetTheme(
                context = this@MainComposeActivity,
                nightMode = nightMode
            ) {
                MainScreen(
                    inCar = get(),
                    appWidgetIdScope = appWidgetIdScope,
                    action = action
                )
//                ThemeColors(listOf(
//                        Triple("primary", MaterialTheme.colors.primary, MaterialTheme.colors.onPrimary),
//                        Triple("secondary", MaterialTheme.colors.secondary, MaterialTheme.colors.onSecondary),
//                        Triple("background", MaterialTheme.colors.background, MaterialTheme.colors.onBackground),
//                ))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appWidgetIdScope?.close()
    }
}

@Composable
fun ThemeColors(colors: List<Triple<String, Color, Color>>) {
    Column(
            modifier = Modifier.padding(64.dp)
    ) {
        colors.forEach { (name, color, on) ->
            Text(
                    modifier = Modifier
                            .background(color)
                            .padding(all = 4.dp)
                            .fillMaxWidth()
                            .height(40.dp),
                    color = on,
                    text = "$name ${color.toColorHex()} on ${on.toColorHex()}"
            )
        }
    }
}
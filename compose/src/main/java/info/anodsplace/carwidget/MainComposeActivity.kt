package info.anodsplace.carwidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.main.MainScreen
import info.anodsplace.compose.LocalPicasso
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

open class MainComposeActivity : AppCompatActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val picasso: Picasso by inject()

    open fun startConfigActivity(appWidgetId: Int) {

    }

    open fun requestWidgetUpdate(appWidgetId: Int) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(appSettings.nightMode)
        super.onCreate(savedInstanceState)
        val appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val action = MutableSharedFlow<UiAction>()
        lifecycleScope.launchWhenResumed {
            action.collect {
                when (it) {
                    is UiAction.OpenWidgetConfig -> startConfigActivity(it.appWidgetId)
                    is UiAction.ApplyWidget -> {
                        val prefs: WidgetInterface = get(parameters = { parametersOf(it.appWidgetId) })
                        prefs.skin = it.skinValue
                        prefs.apply()
                        requestWidgetUpdate(it.appWidgetId)
                        finish()
                    }
                    else -> { }
                }
            }
        }
        setContent {
            val isDarkTheme by appSettings.darkTheme
                .onEach {
                    AppCompatDelegate.setDefaultNightMode(appSettings.nightMode)
                }
                .collectAsState(initial = appSettings.isDarkTheme)
            CarWidgetTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalPicasso provides picasso) {
                    MainScreen(
                        inCar = get(),
                        appWidgetId = appWidgetId,
                        action = action
                    )
                }
            }
        }
    }
}
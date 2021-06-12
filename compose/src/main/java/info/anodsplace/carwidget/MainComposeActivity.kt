package info.anodsplace.carwidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.main.MainScreen
import info.anodsplace.carwidget.utils.LocalPicasso
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class MainComposeActivity : AppCompatActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val picasso: Picasso by inject()

    open fun startConfigActivity(appWidgetId: Int) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(appSettings.nightMode)
        super.onCreate(savedInstanceState)
        val appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

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
                        onOpenWidgetConfig = { appWidgetId -> startConfigActivity(appWidgetId) }
                    )
                }
            }
        }
    }
}
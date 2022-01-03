package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.main.EditShortcut
import info.anodsplace.compose.LocalPicasso
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class OverlayComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val picasso: Picasso by inject()
    private val uiModeManager: UiModeManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(appSettings.nightMode)
        }
        super.onCreate(savedInstanceState)
        val appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val action = MutableSharedFlow<UiAction>()
        lifecycleScope.launchWhenResumed {
            action.collect {
                if (it is UiAction.OnBackNav) {
                    finish()
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
            val pathSegments = intent.data!!.pathSegments
            val args = NavItem.CurrentWidget.EditShortcut.Args(shortcutId = pathSegments[2].toLong(), position = pathSegments[3].toInt())

            CarWidgetTheme(
                    context = this@OverlayComposeActivity,
                    nightMode = nightMode
            ) {
                CompositionLocalProvider(LocalPicasso provides picasso) {
                    EditShortcut(
                            appWidgetId = appWidgetId,
                            args = args,
                            action = action
                    )
                }
            }
        }
    }
}
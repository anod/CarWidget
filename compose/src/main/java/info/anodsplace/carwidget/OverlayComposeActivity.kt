package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.carwidget.screens.shortcuts.EditShortcut
import info.anodsplace.carwidget.screens.widget.EditWidgetButton
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class OverlayComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()
    private var appWidgetIdScope: AppWidgetIdScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(appSettings.uiMode)
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

        val deeplink = Deeplink.match(intent.data!!)
        if (deeplink == null) {
            AppLog.e("Not recognized ${intent.data.toString()}")
            finish()
            return
        }

        appWidgetIdScope = if (deeplink is Deeplink.AppWidgetIdAware) AppWidgetIdScope(deeplink.appWidgetId) else null

        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LaunchedEffect(uiMode) {
                    uiModeManager.setApplicationNightMode(uiMode)
                }
            }

            CarWidgetTheme(
                    context = this@OverlayComposeActivity,
                uiMode = uiMode
            ) {
                when (deeplink) {
                    is Deeplink.EditShortcut -> EditShortcut(
                        appWidgetIdScope = appWidgetIdScope!!,
                        args = NavItem.Tab.CurrentWidget.EditShortcut.Args(shortcutId = deeplink.shortcutId, position = deeplink.position),
                        action = action
                    )
                    is Deeplink.EditWidgetButton -> EditWidgetButton(
                        appWidgetIdScope = appWidgetIdScope!!,
                        args = NavItem.Tab.CurrentWidget.EditWidgetButton.Args(buttonId = deeplink.buttonId),
                        action = action
                    )
                    else -> throw IllegalArgumentException("Unknown deeplink $deeplink")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appWidgetIdScope?.close()
    }
}
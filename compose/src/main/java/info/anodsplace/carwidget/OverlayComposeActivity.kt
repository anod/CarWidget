package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.chooser.ChooserScreen
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.extensions.extras
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.shortcuts.EditShortcut
import info.anodsplace.carwidget.screens.widget.EditWidgetButton
import info.anodsplace.framework.media.MediaKeyEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class OverlayComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()
    private val imageLoader: ImageLoader by inject()
    private var appWidgetIdScope: AppWidgetIdScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(appSettings.uiMode)
        }
        super.onCreate(savedInstanceState)
        val appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

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
                uiMode = uiMode
            ) {
                when (deeplink) {
                    is Deeplink.EditShortcut -> EditShortcut(
                        appWidgetIdScope = appWidgetIdScope!!,
                        args = NavItem.Tab.CurrentWidget.EditShortcut.Args(
                            shortcutId = deeplink.shortcutId,
                            position = deeplink.position
                        ),
                        onDismissRequest = { finish() }
                    )
                    is Deeplink.EditWidgetButton -> EditWidgetButton(
                        appWidgetIdScope = appWidgetIdScope!!,
                        args = NavItem.Tab.CurrentWidget.EditWidgetButton.Args(buttonId = deeplink.buttonId),
                        onDismissRequest = { finish() },
                    )
                    is Deeplink.PlayMediaButton -> PlayMediaButton(
                        onDismissRequest = { finish() },
                        imageLoader = imageLoader
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

@Composable
fun PlayMediaButton(onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { MediaListLoader(context) }
    ChooserScreen(
        modifier = Modifier.padding(16.dp),
        headers = listOf(),
        loader = loader,
        onClick = { entry ->
            if (entry.componentName != null) {
                MediaKeyEvent(context).sendToComponent(
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    entry.componentName,
                    false
                )
            }
            onDismissRequest()
        },
        imageLoader = imageLoader
    )
}
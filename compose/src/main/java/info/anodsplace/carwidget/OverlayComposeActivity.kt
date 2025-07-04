package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.EditWidgetButton
import info.anodsplace.carwidget.chooser.ChooserScreen
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.getOrCreateAppWidgetScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.shortcut.EditShortcut
import info.anodsplace.framework.media.MediaKeyEvent
import info.anodsplace.ktx.extras
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OverlayViewModel(
    appWidgetIdScope: AppWidgetIdScope?
) : WidgetAwareViewModel<Unit, Unit, Unit>(appWidgetIdScope) {

    class Factory(
        private val appWidgetId: Int,
    ) : ViewModelProvider.Factory, KoinComponent {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return OverlayViewModel(
                appWidgetIdScope = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                    getOrCreateAppWidgetScope(appWidgetId) else null
            ) as T
        }
    }

    override fun handleEvent(event: Unit) {}
}

open class OverlayComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()
    private val imageLoader: ImageLoader by inject()
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private val overlayViewModel: OverlayViewModel by viewModels {
        OverlayViewModel.Factory(
            appWidgetId = appWidgetId
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                uiModeManager.setApplicationNightMode(appSettings.uiMode)
            } catch (e: Exception) {
                AppLog.e(e)
            }
        }
        super.onCreate(savedInstanceState)
        appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        val deeplink = Deeplink.match(intent.data!!)
        if (deeplink == null) {
            AppLog.e("Not recognized ${intent.data.toString()}")
            finish()
            return
        }

        appWidgetId = if (deeplink is Deeplink.AppWidgetIdAware) deeplink.appWidgetId else AppWidgetManager.INVALID_APPWIDGET_ID

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
                when (deeplink) {
                    is Deeplink.EditShortcut -> EditShortcut(
                        appWidgetIdScope = overlayViewModel.appWidgetIdScope!!,
                        args = SceneNavKey.EditShortcut(
                            shortcutId = deeplink.shortcutId,
                            position = deeplink.position
                        ),
                        onDismissRequest = { finish() }
                    )
                    is Deeplink.EditWidgetButton -> EditWidgetButton(
                        appWidgetIdScope = overlayViewModel.appWidgetIdScope!!,
                        args = SceneNavKey.EditWidgetButton(buttonId = deeplink.buttonId),
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
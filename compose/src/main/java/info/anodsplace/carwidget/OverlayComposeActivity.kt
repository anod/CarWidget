package info.anodsplace.carwidget

import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.appwidget.EditWidgetButton
import info.anodsplace.carwidget.appwidget.FolderDialog
import info.anodsplace.carwidget.appwidget.PlayMediaButton
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.getOrCreateAppWidgetScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.navigation.SceneNavKey
import info.anodsplace.carwidget.shortcut.EditShortcut
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

    override fun handleEvent(event: Unit) { }
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
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            uiModeManager.setApplicationNightMode(appSettings.uiMode)
        } catch (e: Exception) {
            AppLog.e(e)
        }
        super.onCreate(savedInstanceState)
        appWidgetId = extras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        val deeplink = Deeplink.match(intent)
        if (deeplink == null) {
            AppLog.e("Not recognized ${intent.data.toString()}")
            finish()
            return
        }

        appWidgetId = if (deeplink is Deeplink.AppWidgetIdAware) deeplink.appWidgetId else AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            LaunchedEffect(uiMode) {
                uiModeManager.nightMode = uiMode
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
                    is Deeplink.OpenFolder -> FolderDialog(
                        appWidgetIdScope = overlayViewModel.appWidgetIdScope!!,
                        args = deeplink,
                        onDismissRequest = { finish() },
                        imageLoader = imageLoader
                    )
                    else -> throw IllegalArgumentException("Unknown deeplink $deeplink")
                }
            }
        }
    }
}
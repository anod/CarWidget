package info.anodsplace.carwidget

import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import info.anodsplace.carwidget.content.preferences.AppSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class IconPackComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(appSettings.uiMode)
        }
        super.onCreate(savedInstanceState)
        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LaunchedEffect(uiMode) { uiModeManager.nightMode = uiMode }
            }
            CarWidgetTheme(uiMode = uiMode) {
                IconPackScreen()
            }
        }
    }
}

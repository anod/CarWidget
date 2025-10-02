package info.anodsplace.carwidget

import android.app.UiModeManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.iconpack.IconPackScreen
import info.anodsplace.carwidget.utils.forIconPackResult
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class IconPackComposeActivity : ComponentActivity(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val uiModeManager: UiModeManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        uiModeManager.setApplicationNightMode(appSettings.uiMode)
        super.onCreate(savedInstanceState)
        setContent {
            val uiMode by appSettings.uiModeChange.collectAsState(initial = appSettings.uiMode)
            LaunchedEffect(uiMode) { uiModeManager.nightMode = uiMode }
            CarWidgetTheme(uiMode = uiMode) {
                IconPackScreen(onSelect = { bitmap, resId ->
                    lifecycleScope.launch {
                        val data = Intent().forIconPackResult(
                            icon = bitmap,
                            iconResourceId = resId,
                            uri = null,
                            context = this@IconPackComposeActivity
                        )
                        setResult(RESULT_OK, data)
                        finish()
                    }
                })
            }
        }
    }
}

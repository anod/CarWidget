package info.anodsplace.carwidget.prefs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import info.anodsplace.carwidget.compose.BackPressedDispatcherAmbient
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.carwidget.compose.intent.IntentEditScreen
import info.anodsplace.framework.AppLog

class IntentEditActivity : AppCompatActivity() {

    companion object {
        const val extraUri = "launchUri"
    }

    private val viewModel: IntentEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extraUri = intent.getStringExtra(extraUri)
        var shortcutIntent: Intent? = null
        try {
            shortcutIntent = Intent.parseUri(extraUri, 0)
        } catch (e: Exception) {
            AppLog.e(e)
        }
        if (shortcutIntent == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        viewModel.intent.value = shortcutIntent

        setContent {
            Providers(BackPressedDispatcherAmbient provides this) {
                CarWidgetTheme {
                    IntentEditScreen(viewModel.intent, viewModel.action)
                }
            }
        }

        viewModel.action.observe(this, {
            when (it) {
                is UiAction.OnBackNav -> finish()
                is UiAction.IntentEditAction -> { }
            }
        })
    }

}
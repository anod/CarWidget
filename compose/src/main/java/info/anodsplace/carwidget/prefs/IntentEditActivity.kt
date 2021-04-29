package info.anodsplace.carwidget.prefs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import info.anodsplace.carwidget.compose.LocalBackPressedDispatcher
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.UiAction
import info.anodsplace.carwidget.compose.intent.IntentEditScreen
import info.anodsplace.carwidget.compose.intent.UpdateField
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.flow.collect

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
            CompositionLocalProvider(LocalBackPressedDispatcher provides this) {
                CarWidgetTheme {
                    IntentEditScreen(viewModel.intent, viewModel.action, addBackPressHandler = true)
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.action.collect {
                when (it) {
                    is UiAction.OnBackNav -> finish()
                    is UpdateField -> {
                        viewModel.updateField(it.field)
                    }
                    is UiAction.IntentEditAction -> {

                    }
                }
            }
        }
    }

}
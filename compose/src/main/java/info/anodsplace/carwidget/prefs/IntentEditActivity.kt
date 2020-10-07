package info.anodsplace.carwidget.prefs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.compose.IntentEditView

class IntentEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarWidgetTheme {
                IntentEditView(intent = intent)
            }
        }
    }

}
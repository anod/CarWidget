package info.anodsplace.carwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.screens.MainScreen
import info.anodsplace.carwidget.utils.LocalPicasso
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

open class MainComposeActivity : ComponentActivity(), KoinComponent {
    val appSettings: AppSettings by inject()
    val picasso: Picasso by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var isDarkTheme by remember { mutableStateOf(appSettings.isDarkTheme) }
            CarWidgetTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalPicasso provides picasso) {
                    MainScreen(inCar = get())
                }
            }
            coroutineScope.launch {
                appSettings.changes.collect { (key, _) ->
                    if (key == AppSettings.APP_THEME) {
                        isDarkTheme = appSettings.isDarkTheme
                    }
                }
            }
        }
    }
}
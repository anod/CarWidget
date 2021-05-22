package info.anodsplace.carwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.screens.MainScreen
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class MainComposeActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CarWidgetTheme {
                MainScreen(inCar = get())
            }
        }
    }
}
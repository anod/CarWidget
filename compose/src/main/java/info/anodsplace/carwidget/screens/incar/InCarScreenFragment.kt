package info.anodsplace.carwidget.screens.incar

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.screens.NavItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InCarScreenFragment : Fragment(), KoinComponent {
    private val inCar: InCarInterface by inject()
    private val appSettings: AppSettings by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = inflater.inflate(R.layout.fragment_compose_view, container, false) as ComposeView
        val appWidgetId = requireArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        return composeView.apply {
            setContent {
                val navController = rememberNavController()
                val isDarkTheme by remember { mutableStateOf(appSettings.isDarkTheme) }
                CarWidgetTheme(darkTheme = isDarkTheme) {
                    Surface(
                        color = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground
                    ) {
                        NavHost(navController, startDestination = NavItem.InCar.route) {
                            inCarNavigation(inCar, navController = navController, innerPadding = PaddingValues())
                        }
                    }
                }
            }
        }
    }
}
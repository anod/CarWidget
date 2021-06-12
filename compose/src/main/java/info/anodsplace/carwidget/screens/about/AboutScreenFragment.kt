package info.anodsplace.carwidget.screens.about

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.preferences.AppSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AboutScreenFragment : Fragment(), KoinComponent {
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
                val isDarkTheme by appSettings.darkTheme
                    .onEach {
                        AppCompatDelegate.setDefaultNightMode(appSettings.nightMode)
                    }
                    .collectAsState(initial = appSettings.isDarkTheme)
                CarWidgetTheme(darkTheme = isDarkTheme) {
                    Surface(
                        color = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground
                    ) {
                        val aboutViewModel: AboutViewModel = viewModel()
                        val aboutScreenState by aboutViewModel.initScreenState(appWidgetId = appWidgetId).collectAsState()
                        AboutScreen(aboutScreenState, aboutViewModel.uiAction, modifier = Modifier.padding(PaddingValues()))
                    }
                }
            }
        }
    }
}
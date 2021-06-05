package info.anodsplace.carwidget.screens.about

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.R

class AboutScreenFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = inflater.inflate(R.layout.fragment_compose_view, container, false) as ComposeView
        val appWidgetId = requireArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        return composeView.apply {
            setContent {
                val aboutViewModel: AboutViewModel = viewModel()
                val aboutScreenState by aboutViewModel.initScreenState(appWidgetId = appWidgetId).collectAsState()
                AboutScreen(aboutScreenState, aboutViewModel.uiAction, modifier = Modifier.padding(PaddingValues()))
            }
        }
    }
}
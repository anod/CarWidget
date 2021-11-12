package info.anodsplace.carwidget.screens.shortcuts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StyleRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.compose.LocalPicasso
import info.anodsplace.framework.app.FragmentContainerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShortcutNewFragment : Fragment(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val picasso: Picasso by inject()

    class Factory(
        val position: Int,
        val appWidgetId: Int,
        @StyleRes themeResId: Int
    ) : FragmentContainerFactory(fragmentTag = "shortcut-new", themeResId = themeResId) {
        override fun create() = ShortcutNewFragment().apply {
            arguments = bundleOf(
                ShortcutEditFragment.extraPosition to position,
                ShortcutEditFragment.extraAppWidgetId to appWidgetId
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val nightMode by appSettings.nightModeChange.collectAsState(initial = appSettings.nightMode)
                CarWidgetTheme(
                    context = requireContext(),
                    nightMode = nightMode
                ) {
                    CompositionLocalProvider(LocalPicasso provides picasso) {
                        val loader by remember { mutableStateOf(QueryIntentLoader(context, Intent(Intent.ACTION_CREATE_SHORTCUT))) }
                        ChooserDialog(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            loader = loader,
                            headers = listOf(
                                Header(stringResource(R.string.applications), iconVector = Icons.Filled.List),
                                Header(stringResource(R.string.car_widget_shortcuts), iconVector = Icons.Filled.List),
                            ),
                            onDismissRequest = {  },
                            onClick = {

                            }
                        )
                    }
                }
            }
        }
    }
}
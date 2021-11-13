package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StyleRes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.shortcuts.ShortcutInfoUtils
import info.anodsplace.compose.LocalPicasso
import info.anodsplace.framework.app.FragmentContainerFactory
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShortcutPickerFragment : Fragment(), KoinComponent {
    private val appSettings: AppSettings by inject()
    private val picasso: Picasso by inject()

    class Factory(
        val position: Int,
        val appWidgetId: Int,
        @StyleRes themeResId: Int
    ) : FragmentContainerFactory(fragmentTag = "shortcut-new", themeResId = themeResId) {
        override fun create() = ShortcutPickerFragment().apply {
            arguments = bundleOf(
                ShortcutEditFragment.extraPosition to position,
                ShortcutEditFragment.extraAppWidgetId to appWidgetId
            )
        }
    }

    private val viewModel: ShortcutPickerViewModel by viewModels { ShortcutPickerViewModel.Factory(
        requireArguments().getInt(ShortcutEditFragment.extraPosition),
        requireArguments().getInt(ShortcutEditFragment.extraAppWidgetId),
        requireContext().applicationContext as Application
    ) }

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
                        ShortcutPickerScreen(viewModel) {
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.actions.collect {
                when (it) {
                    ShortcutPickerAction.SavedError -> {

                    }
                    is ShortcutPickerAction.SavedSuccess -> {
                        if (it.resultCode == ShortcutInfoUtils.successAppShortcut) {
                            Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_SHORT).show()
                        } else if (it.resultCode == ShortcutInfoUtils.failedAppShortcut) {
                            Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_LONG).show()
                        }
                        activity?.finish()
                    }
                }
            }
        }
    }
}
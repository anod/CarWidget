package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import info.anodsplace.compose.LocalPicasso
import info.anodsplace.framework.app.FragmentContainerFactory
import kotlinx.coroutines.flow.collect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShortcutEditFragment : Fragment(), KoinComponent {
    private val picasso: Picasso by inject()

    companion object {
        const val extraShortcutId = "extra_shortcut_id"
        const val extraPosition = "extra_position"
        const val extraAppWidgetId = "extra_app_widget_id"
    }

    class Factory(
            val position: Int,
            val shortcutId: Long,
            val appWidgetId: Int
    ) : FragmentContainerFactory(fragmentTag = "shortcut-edit") {
        override fun create() = ShortcutEditFragment().apply {
            arguments = bundleOf(
                    extraShortcutId to shortcutId,
                    extraPosition to position,
                    extraAppWidgetId to appWidgetId
            )
        }
    }

    private val viewModel: ShortcutEditViewModel by viewModels { ShortcutEditViewModel.Factory(
            requireArguments().getInt(extraPosition),
            requireArguments().getLong(extraShortcutId),
            requireArguments().getInt(extraAppWidgetId),
            requireContext().applicationContext as Application
    ) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CompositionLocalProvider(LocalPicasso provides picasso) {
                    ShortcutEditDialog(
                        shortcut = viewModel.shortcut,
                        action = viewModel.actions
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.actions.collect {
                when (it) {
                    ShortcutEditAction.Drop -> {
                        viewModel.drop()
                        activity?.finish()
                    }
                    ShortcutEditAction.Ok -> {
                        activity?.finish()
                    }
                }
            }
        }
    }
}
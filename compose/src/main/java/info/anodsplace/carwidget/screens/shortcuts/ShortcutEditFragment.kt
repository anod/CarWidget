package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import info.anodsplace.carwidget.R
import info.anodsplace.framework.app.FragmentContainerFactory

class ShortcutEditFragment : Fragment() {

    companion object {
        const val extraShortcutId = "extra_shortcut_id"
        const val extraPosition = "extra_position"
        const val extraAppWidgetId = "extra_app_widget_id"
    }

    class Factory(
            val position: Int,
            val shortcutId: Long,
            val appWidgetId: Int
    ) : FragmentContainerFactory(fragmentTag = "shortcut-edit", themeResId = R.style.Theme_AppCompat_DayNight_Dialog) {
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
        return inflater.inflate(R.layout.fragment_compose_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView
        composeView.apply {
            // Dispose the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ShortcutEditDialog(
                        cellId = viewModel.position,
                        shortcut = viewModel.shortcut,
                        icon = viewModel.icon
                )
            }
        }
    }
}
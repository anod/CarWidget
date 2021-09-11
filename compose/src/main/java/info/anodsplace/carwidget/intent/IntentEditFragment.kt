package info.anodsplace.carwidget.intent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.compose.LocalBackPressedDispatcher
import info.anodsplace.carwidget.screens.UiAction
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.FragmentContainerFactory
import kotlinx.coroutines.flow.collect

class IntentEditFragment: Fragment() {

    class Factory(private val intentUri: String): FragmentContainerFactory("IntentEditFragment") {
        override fun create() = IntentEditFragment().apply {
            arguments = bundleOf(extraUri to intentUri)
        }
    }

    companion object {
        private const val extraUri = "launchUri"
    }

    private val viewModel: IntentEditViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.intent.value = try {
            Intent.parseUri(requireArguments().getString(extraUri), 0) ?: Intent()
        } catch (e: Exception) {
            AppLog.e(e)
            Intent()
        }

        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(LocalBackPressedDispatcher provides requireActivity()) {
                    CarWidgetTheme {
                        IntentEditScreen(viewModel.intent, viewModel.action, addBackPressHandler = true)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed {
            viewModel.action.collect {
                when (it) {
                    is UiAction.OnBackNav -> { parentFragmentManager.popBackStack() }
                    is UpdateField -> {
                        viewModel.updateField(it.field)
                    }
                    is UiAction.IntentEditAction -> {

                    }
                }
            }
        }
    }
}
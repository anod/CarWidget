package info.anodsplace.carwidget.screens.shortcuts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.screens.NavItem

@Composable
fun EditShortcut(appWidgetIdScope: AppWidgetIdScope, args: NavItem.Tab.CurrentWidget.EditShortcut.Args, onDismissRequest: () -> Unit) {
    if (args.shortcutId > 0) {
        val viewModel: ShortcutEditViewModel = viewModel(
            factory = ShortcutEditViewModel.Factory(
                args.position,
                args.shortcutId,
                appWidgetIdScope,
            )
        )
        val state by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
        ShortcutEditScreen(
            state = state,
            onEvent = { viewModel.handleEvent(it) } ,
            onDismissRequest = onDismissRequest,
            widgetSettings = viewModel.widgetSettings,
            imageLoader = viewModel.imageLoader
        )
    } else {
        val viewModel: ShortcutPickerViewModel = viewModel(
            factory = ShortcutPickerViewModel.Factory(
                args.position,
                appWidgetIdScope,
            )
        )
        ShortcutPickerScreen(
            viewModel.viewActions,
            onEvent = { viewModel.handleEvent(it) },
            onDismissRequest = onDismissRequest,
            shortcutResources = viewModel.shortcutResources,
            imageLoader = viewModel.imageLoader
        )
    }
}
package info.anodsplace.carwidget.shortcut

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.NavItem
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.framework.content.CommonActivityAction

@Composable
fun EditShortcut(
    appWidgetIdScope: AppWidgetIdScope,
    args: NavItem.Tab.CurrentWidget.EditShortcut.Args,
    onActivityAction: (CommonActivityAction) -> Unit,
    onDismissRequest: () -> Unit
) {
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
            onEvent = viewModel::handleEvent,
            onDismissRequest = onDismissRequest,
            widgetSettings = viewModel.widgetSettings,
            imageLoader = viewModel.imageLoader
        )
        LaunchedEffect(key1 = true) {
            viewModel.viewActions.collect {
                when (it) {
                    is ShortcutEditViewAction.ActivityAction -> onActivityAction(it.action)
                }
            }
        }
    } else {
        val viewModel: ShortcutPickerViewModel = viewModel(
            factory = ShortcutPickerViewModel.Factory(
                args.position,
                appWidgetIdScope,
            )
        )
        ShortcutPickerScreen(
            onEvent = viewModel::handleEvent,
            onDismissRequest = onDismissRequest,
            shortcutResources = viewModel.shortcutResources,
            imageLoader = viewModel.imageLoader
        )

        LaunchedEffect(true) {
            viewModel.viewActions.collect { action ->
                when (action) {
                    is ShortcutPickerViewAction.ActivityAction -> onActivityAction(action.action)
                }
            }
        }
    }
}
package info.anodsplace.carwidget.shortcut

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.unaryPlus
import info.anodsplace.carwidget.navigation.SceneNavKey
import info.anodsplace.framework.content.showToast

@Composable
fun EditShortcut(
    appWidgetIdScope: AppWidgetIdScope,
    args: SceneNavKey.EditShortcut,
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
            imageLoader = viewModel.imageLoader,
            shortcutResources = viewModel.shortcutResources
        )
        val context = LocalContext.current
        LaunchedEffect(key1 = true) {
            viewModel.viewActions.collect {
                when (it) {
                    is ShortcutEditViewAction.ShowToast -> context.showToast(it)
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
            imageLoader = viewModel.imageLoader,
            appWidgetId = +appWidgetIdScope
        )
        val context = LocalContext.current
        LaunchedEffect(true) {
            viewModel.viewActions.collect { action ->
                when (action) {
                    is ShortcutPickerViewAction.ShowToast -> context.showToast(action)
                }
            }
        }
    }
}
package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun EditShortcut(appWidgetIdScope: AppWidgetIdScope, args: NavItem.CurrentWidget.EditShortcut.Args, action: MutableSharedFlow<UiAction>) {
    val appContext = LocalContext.current.applicationContext as Application
    val scope = rememberCoroutineScope()

    if (args.shortcutId > 0) {
        val viewModel: ShortcutEditViewModel = viewModel(
            factory = ShortcutEditViewModel.Factory(
                args.position,
                args.shortcutId,
                appWidgetIdScope,
                appContext
            )
        )
        ShortcutEditScreen(viewModel) {
            scope.launch { action.emit(UiAction.OnBackNav) }
        }
    } else {
        val viewModel: ShortcutPickerViewModel = viewModel(
            factory = ShortcutPickerViewModel.Factory(
                args.position,
                appWidgetIdScope,
                appContext
            )
        )
        ShortcutPickerScreen(viewModel) {
            scope.launch { action.emit(UiAction.OnBackNav) }
        }
    }
}
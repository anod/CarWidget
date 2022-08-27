package info.anodsplace.carwidget.screens.shortcuts

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.screens.NavItem

@Composable
fun EditShortcut(appWidgetIdScope: AppWidgetIdScope, args: NavItem.Tab.CurrentWidget.EditShortcut.Args, onDismissRequest: () -> Unit) {
    val appContext = LocalContext.current.applicationContext as Application

    if (args.shortcutId > 0) {
        val viewModel: ShortcutEditViewModel = viewModel(
            factory = ShortcutEditViewModel.Factory(
                args.position,
                args.shortcutId,
                appWidgetIdScope,
                appContext
            )
        )
        ShortcutEditScreen(viewModel, onDismissRequest = onDismissRequest)
    } else {
        val viewModel: ShortcutPickerViewModel = viewModel(
            factory = ShortcutPickerViewModel.Factory(
                args.position,
                appWidgetIdScope,
                appContext
            )
        )
        ShortcutPickerScreen(viewModel, onDismissRequest = onDismissRequest)
    }
}
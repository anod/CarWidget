package info.anodsplace.carwidget.screens.widget

import android.app.Application
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.chooser.ChooserGridList
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.screens.NavItem
import info.anodsplace.carwidget.screens.UiAction

@Composable
fun EditWidgetButton(appWidgetIdScope: AppWidgetIdScope, args: NavItem.Tab.CurrentWidget.EditWidgetButton.Args, action: (UiAction) -> Unit) {
    val appContext = LocalContext.current.applicationContext as Application
    val scope = rememberCoroutineScope()

    val viewModel: EditWidgetViewModel = viewModel(
        factory = EditWidgetViewModel.Factory(
            appContext,
            appWidgetIdScope,
            args.buttonId
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 352.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        ChooserGridList(emptyList(), viewModel.items, onClick = { entry ->
            viewModel.onSelect(entry)
            action(UiAction.OnBackNav)
        })
    }
}
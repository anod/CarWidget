package info.anodsplace.carwidget.appwidget

import android.app.Application
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.chooser.ChooserAsyncImage
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.navigation.SceneNavKey
import info.anodsplace.compose.chooser.ChooserGridList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun EditWidgetButton(appWidgetIdScope: AppWidgetIdScope, args: SceneNavKey.EditWidgetButton, onDismissRequest: () -> Unit) {
    val appContext = LocalContext.current.applicationContext as Application

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
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        ChooserGridList(
            headers = persistentListOf(),
            list = viewModel.viewState.items,
            onSelect = { entry ->
                viewModel.handleEvent(EditWidgetViewEvent.Select(entry))
                onDismissRequest()
            },
            asyncImage = { entry, colorFilter ->
                ChooserAsyncImage(
                    entry = entry,
                    colorFilter = colorFilter,
                    imageLoader = viewModel.imageLoader
                )
            },
        )
    }
}
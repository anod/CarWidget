package info.anodsplace.carwidget.appwidget

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserGridListDefaults
import info.anodsplace.carwidget.chooser.IntentsChooserLoader
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.framework.content.startActivitySafely

/**
 * Displays a dialog listing intents contained in a folder shortcut.
 * Expects parent Activity intent extras to contain:
 *  - Intent.EXTRA_SHORTCUT_NAME (folder title)
 *  - EXTRA_FOLDER_ITEM_URIS (ArrayList<String>) new format, OR
 *  - EXTRA_FOLDER_ITEMS (ArrayList<Intent>) legacy fallback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDialog(
    args: Deeplink.OpenFolder,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    appWidgetIdScope: AppWidgetIdScope
) {
    val appContext = LocalContext.current.applicationContext as Application
    val viewModel: FolderDialogViewModel = viewModel(
        factory = FolderDialogViewModel.Factory(
            appWidgetIdScope = appWidgetIdScope,
            position = args.position,
            extras = args.extras,
            appContext = appContext
        )
    )
    val context = LocalContext.current
    val viewState by viewModel.viewStates.collectAsState()
    val loader = remember(viewState.intents) { IntentsChooserLoader(context, viewState.intents) }

    ChooserDialog(
        loader = loader,
        headers = emptyList(),
        onDismissRequest = onDismissRequest,
        imageLoader = imageLoader,
        onClick = { entry ->
            val launch = entry.getIntent(null)
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivitySafely(launch)
            onDismissRequest()
        },
        style = ChooserGridListDefaults.singleSelect(),
        modifier = Modifier.padding(16.dp),
        topContent = { _ ->
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = viewState.title, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    )
}

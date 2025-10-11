package info.anodsplace.carwidget.shortcut

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import info.anodsplace.carwidget.chooser.AllAppsIntentChooserLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.QueryIntentChooserLoader
import info.anodsplace.carwidget.chooser.headerEntry
import info.anodsplace.carwidget.chooser.headerId
import info.anodsplace.carwidget.chooser.isHeader
import info.anodsplace.carwidget.chooser.toShortcutIntent
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.LaunchShortcutError
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.Save
import info.anodsplace.carwidget.utils.forPickShortcutLocal

sealed interface ShortcutPickerState {
    data object Apps: ShortcutPickerState
    data object Shortcuts: ShortcutPickerState
    data object Folder: ShortcutPickerState
}

@Composable
fun ShortcutPickerScreen(
    onEvent: (ShortcutPickerViewEvent) -> Unit,
    onDismissRequest: () -> Unit,
    shortcutResources: ShortcutResources,
    imageLoader: ImageLoader
) {
    var screenState by remember { mutableStateOf<ShortcutPickerState>(ShortcutPickerState.Apps) }
    val activityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                onEvent(Save(ShortcutIntent(result.data!!, isApp = false)))
            }
            onDismissRequest()
    })

    when (screenState) {
        ShortcutPickerState.Apps -> AppChooser(
            onNewState = { screenState = it },
            onChoose = { entry ->
                onEvent(Save(entry.toShortcutIntent(isApp = true)))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
            imageLoader = imageLoader
        )
        ShortcutPickerState.Shortcuts -> ShortcutChooser(
            onIntent = {
                try {
                    activityLauncher.launch(it)
                } catch (e: Exception) {
                    onEvent(LaunchShortcutError(exception = e))
                }
            },
            onChooseHeader = { entry ->
                onEvent(Save(entry.toShortcutIntent(isApp = false)))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Apps },
            imageLoader = imageLoader,
            shortcutResources = shortcutResources
        )
        ShortcutPickerState.Folder -> FolderChooser(
            onSave = { folderIntent, items ->
                onEvent(ShortcutPickerViewEvent.SaveFolder(folderIntent, items))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Apps },
            imageLoader = imageLoader,
            shortcutResources = shortcutResources
        )
    }
}

@Composable
private fun ShortcutChooser(
    onIntent: (Intent) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    shortcutResources: ShortcutResources,
    onChooseHeader: (ChooserEntry) -> Unit
) {
    val context = LocalContext.current
    val baseIntent = remember { Intent(Intent.ACTION_CREATE_SHORTCUT) }
    val loader = remember { QueryIntentChooserLoader(context, baseIntent) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = createCarWidgetShortcuts(context, shortcutResources),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when {
                entry.isHeader -> onChooseHeader(entry)
                else -> onIntent(entry.getIntent(baseIntent = baseIntent))
            }
        },
        imageLoader = imageLoader
    )
}

@Composable
private fun AppChooser(
    onNewState: (ShortcutPickerState) -> Unit,
    onChoose: (ChooserEntry) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { AllAppsIntentChooserLoader(context) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = listOf(
            headerEntry(0,
                stringResource(R.string.shortcuts),
                iconVector = Icons.AutoMirrored.Filled.AltRoute
            ),
            headerEntry(1,
                stringResource(R.string.folder),
                iconVector = Icons.Outlined.Folder
            ),
        ),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when {
                entry.isHeader -> {
                    if (entry.headerId == 0) {
                        onNewState(ShortcutPickerState.Shortcuts)
                    } else if (entry.headerId == 1) {
                        onNewState(ShortcutPickerState.Folder)
                    }
                }
                else -> onChoose(entry)
            }
        },
        imageLoader = imageLoader
    )
}
private fun createCarWidgetShortcuts(context: Context, shortcutResources: ShortcutResources): List<ChooserEntry> {
    val shortcuts = InternalShortcut.all
    val titles = InternalShortcut.titles(context)
    return shortcuts.map { shortcut ->
        val title = titles[shortcut.index]
        val icon = shortcutResources.internalShortcuts.icons[shortcut.index]
        val intent = Intent().forPickShortcutLocal(shortcut, title, icon, context, shortcutResources)
        headerEntry(headerId = shortcut.index, title = title, iconRes = icon, intent = intent)
    }
}

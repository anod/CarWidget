package info.anodsplace.carwidget.shortcut

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.Shortcut
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
import coil3.ImageLoader
import info.anodsplace.carwidget.chooser.AllAppsIntentChooserLoader
import info.anodsplace.carwidget.chooser.AllWidgetShortcutsChooserLoader
import info.anodsplace.carwidget.chooser.ChooserAsyncImage
import info.anodsplace.carwidget.chooser.ChooserEmptyState
import info.anodsplace.carwidget.chooser.isAppEntry
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.LaunchShortcutError
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.Save
import info.anodsplace.carwidget.utils.forPickShortcutLocal
import info.anodsplace.compose.chooser.ChooserDialog
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.QueryIntentChooserLoader
import info.anodsplace.compose.chooser.headerEntry
import info.anodsplace.compose.chooser.headerId
import info.anodsplace.compose.chooser.isHeader
import info.anodsplace.ktx.resourceUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

sealed interface ShortcutPickerState {
    data object Apps: ShortcutPickerState
    data object Shortcuts: ShortcutPickerState
    data object Folder: ShortcutPickerState
    data object Existing: ShortcutPickerState
}

@Composable
fun ShortcutPickerScreen(
    onEvent: (ShortcutPickerViewEvent) -> Unit,
    onDismissRequest: () -> Unit,
    shortcutResources: ShortcutResources,
    imageLoader: ImageLoader,
    appWidgetId: Int // current widget id for ordering existing shortcuts
) {
    var screenState by remember { mutableStateOf<ShortcutPickerState>(ShortcutPickerState.Apps) }
    val activityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val intent = result.data!!
                val entry = ChooserEntry(
                    componentName = intent.component,
                    title = "",
                    intent = intent
                )
                onEvent(Save(entry))
            }
            onDismissRequest()
    })

    when (screenState) {
        ShortcutPickerState.Apps -> AppChooser(
            onNewState = { screenState = it },
            onChoose = { entry ->
                entry.isAppEntry = true
                onEvent(Save(entry))
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
                onEvent(Save(entry))
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
        ShortcutPickerState.Existing -> ExistingShortcutChooser(
            appWidgetId = appWidgetId,
            onChoose = { entry ->
                onEvent(Save(entry))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Apps },
            imageLoader = imageLoader
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
        asyncImage = { entry, colorFilter -> ChooserAsyncImage(entry, colorFilter, imageLoader) },
        emptyState = { filterApplied -> ChooserEmptyState(filterApplied) },
    )
}

@Composable
private fun AppChooser(
    onNewState: (ShortcutPickerState) -> Unit,
    onChoose: (ChooserEntry) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current
    val loader = remember { AllAppsIntentChooserLoader(context) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = persistentListOf(
            headerEntry(0,
                stringResource(R.string.shortcuts),
                iconVector = Icons.AutoMirrored.Filled.AltRoute
            ),
            headerEntry(1,
                stringResource(R.string.folder),
                iconVector = Icons.Outlined.Folder
            ),
            headerEntry(2,
                stringResource(R.string.existing_shortcut),
                iconVector = Icons.AutoMirrored.Filled.Shortcut
            ),
        ),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when {
                entry.isHeader -> {
                    when (entry.headerId) {
                        0 -> onNewState(ShortcutPickerState.Shortcuts)
                        1 -> onNewState(ShortcutPickerState.Folder)
                        2 -> onNewState(ShortcutPickerState.Existing)
                    }
                }
                else -> onChoose(entry)
            }
        },
        asyncImage = { entry, colorFilter -> ChooserAsyncImage(entry, colorFilter, imageLoader) },
        emptyState = { filterApplied -> ChooserEmptyState(filterApplied) },
    )
}

@Composable
private fun ExistingShortcutChooser(
    appWidgetId: Int,
    onChoose: (ChooserEntry) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current
    val loader = remember(appWidgetId) {
        AllWidgetShortcutsChooserLoader(context, appWidgetId,)
    }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = persistentListOf(), // headers produced by loader as entries with headerId
        onDismissRequest = onDismissRequest,
        onClick = { entry -> if (!entry.isHeader) onChoose(entry) },
        loadingSection = AllWidgetShortcutsChooserLoader.sectionForWidget(appWidgetId, context),
        asyncImage = { entry, colorFilter -> ChooserAsyncImage(entry, colorFilter, imageLoader) },
        emptyState = { filterApplied -> ChooserEmptyState(filterApplied) },
    )
}

private fun createCarWidgetShortcuts(context: Context, shortcutResources: ShortcutResources): ImmutableList<ChooserEntry> {
    val shortcuts = InternalShortcut.all
    val titles = InternalShortcut.titles(context)
    return shortcuts.map { shortcut ->
        val title = titles[shortcut.index]
        val icon = shortcutResources.internalShortcuts.icons[shortcut.index]
        val intent = Intent().forPickShortcutLocal(shortcut, title, icon, context, shortcutResources)
        headerEntry(headerId = shortcut.index, title = title, iconUri = context.resourceUri(icon), intent = intent)
    }.toImmutableList()
}

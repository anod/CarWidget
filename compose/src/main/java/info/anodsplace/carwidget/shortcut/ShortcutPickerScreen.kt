package info.anodsplace.carwidget.shortcut

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import info.anodsplace.carwidget.chooser.AllAppsIntentLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserEntry
import info.anodsplace.carwidget.chooser.ChooserGridListDefaults
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MultiSelectChooserDialog
import info.anodsplace.carwidget.chooser.QueryIntentLoader
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.shortcuts.InternalShortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.LaunchShortcutError
import info.anodsplace.carwidget.shortcut.ShortcutPickerViewEvent.Save
import info.anodsplace.carwidget.utils.forFolder
import info.anodsplace.carwidget.utils.forPickShortcutLocal
import info.anodsplace.compose.PickGroup
import info.anodsplace.ktx.sortAndReorder

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
                onEvent(Save(result.data!!, isApp = false))
            }
            onDismissRequest()
    })

    when (screenState) {
        ShortcutPickerState.Apps -> AppChooser(
            onNewState = { screenState = it },
            onChoose = { entry ->
                onEvent(Save(entry.getIntent(baseIntent = null), isApp = true))
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
                onEvent(Save(entry.getIntent(baseIntent = null), isApp = false))
                onDismissRequest()
            },
            onDismissRequest = { screenState = ShortcutPickerState.Apps },
            imageLoader = imageLoader,
            shortcutResources = shortcutResources
        )
        ShortcutPickerState.Folder -> FolderChooser(
            onCreate = { folderIntent, items ->
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
fun FolderChooser(
    onCreate: (Intent, List<ChooserEntry>) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    shortcutResources: ShortcutResources
) {
    val context = LocalContext.current
    val loader = remember { AllAppsIntentLoader(context) }
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<ComponentName>()) }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }

    MultiSelectChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = emptyList(),
        selectedComponents = selected,
        style = ChooserGridListDefaults.multiSelect().copy(grayscaleUnselectedIcons = true),
        onSelect = { entry ->
            val component = entry.componentName ?: return@MultiSelectChooserDialog
            selected = if (selected.contains(component)) selected - component else selected + component
        },
        onDismissRequest = onDismissRequest,
        imageLoader = imageLoader,
        topContent = { apps ->
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(id = R.string.title)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryFilterChips(
                    apps = apps,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
        },
        bottomContent = { apps ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = android.R.string.cancel))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val selectedEntries = apps.filter { it.componentName != null && selected.contains(it.componentName) }
                        val folderIntent = Intent().forFolder(
                            title = title,
                            ctx = context,
                            target = shortcutResources
                        )
                        onCreate(folderIntent, selectedEntries)
                    },
                    enabled = selected.isNotEmpty()
                ) {
                    Text(stringResource(id = R.string.create))
                }
            }
        },
        listFilter = if (selectedCategory == null) { { it } } else {
            { list -> list.filter { it.category == selectedCategory } }
        }
    )
}

@Composable
private fun CategoryFilterChips(
    apps: List<ChooserEntry>,
    selectedCategory: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    val context = LocalContext.current
    val (categoryNames, orderedCategoryIds) = categoryNamesAndIds(context, apps)
    if (categoryNames.isNotEmpty()) {
        val allText = stringResource(id = R.string.all)
        val options = remember(categoryNames) { arrayOf(allText) + categoryNames.toTypedArray() }
        val selectedIndex = remember(selectedCategory, orderedCategoryIds) {
            if (selectedCategory == null) 0 else (orderedCategoryIds.indexOf(selectedCategory) + 1).coerceAtLeast(0)
        }
        PickGroup(
            options = options,
            selectedIndex = selectedIndex,
            onValueChanged = { idx ->
                onCategorySelected(if (idx == 0) null else orderedCategoryIds.getOrNull(idx - 1))
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
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
    val loader = remember { QueryIntentLoader(context, baseIntent) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = createCarWidgetShortcuts(context, shortcutResources),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when(entry) {
                is Header -> onChooseHeader(entry)
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
    val loader = remember { AllAppsIntentLoader(context) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        headers = listOf(
            Header(0,
                stringResource(R.string.shortcuts),
                iconVector = Icons.AutoMirrored.Filled.AltRoute
            ),
            Header(1,
                stringResource(R.string.folder),
                iconVector = Icons.Outlined.Folder
            ),
        ),
        onDismissRequest = onDismissRequest,
        onClick = { entry ->
            when(entry) {
                is Header -> {
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

@Composable
fun IntentChooser(intent: Intent, onChoose: (ChooserEntry) -> Unit, onDismissRequest: () -> Unit, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val loader = remember { QueryIntentLoader(context, intent) }
    ChooserDialog(
        modifier = Modifier.padding(horizontal = 16.dp),
        loader = loader,
        onDismissRequest = onDismissRequest,
        onClick = onChoose,
        imageLoader = imageLoader
    )
}

private fun createCarWidgetShortcuts(context: Context, shortcutResources: ShortcutResources): List<Header> {
    val shortcuts = InternalShortcut.all
    val titles = InternalShortcut.titles(context)
    return shortcuts.map { shortcut ->
        val title = titles[shortcut.index]
        val icon = shortcutResources.internalShortcuts.icons[shortcut.index]
        val intent = Intent().forPickShortcutLocal(shortcut, title, icon, context, shortcutResources)
        Header(headerId = shortcut.index, title = title, iconRes = icon, intent = intent)
    }
}

// Extracted helper producing category names (display order) and ordered category ids from the provided apps list
private fun categoryNamesAndIds(context: Context, apps: List<ChooserEntry>): Pair<List<String>, List<Int>> {
    val categoryIds = apps.map { it.category }.distinct()
    return categoryIds.sortAndReorder(categoryIds) { id ->
        ApplicationInfo.getCategoryTitle(context, id)?.toString()
    }
}

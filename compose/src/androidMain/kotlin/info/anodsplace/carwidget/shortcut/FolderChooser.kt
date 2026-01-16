package info.anodsplace.carwidget.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import info.anodsplace.carwidget.chooser.AllAppsIntentChooserLoader
import info.anodsplace.carwidget.chooser.ChooserAsyncImage
import info.anodsplace.carwidget.chooser.ChooserEmptyState
import info.anodsplace.carwidget.chooser.ShortcutsChooserLoader
import info.anodsplace.carwidget.chooser.isAppEntry
import info.anodsplace.carwidget.chooser.toShortcutIntent
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntent
import info.anodsplace.carwidget.content.shortcuts.ShortcutResources
import info.anodsplace.carwidget.utils.forFolder
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.ChooserGridListDefaults
import info.anodsplace.compose.chooser.CompositeChooserLoader
import info.anodsplace.compose.chooser.MultiSelectChooserDialog

/**
 * Folder item picker used both for creating a new folder and editing an existing one.
 * initialSelectedComponents currently unused for editing because existing folder items
 * are not yet exposed via ViewModel (future enhancement).
 */
@Composable
fun FolderChooser(
    onSave: (ShortcutIntent, List<ShortcutIntent>) -> Unit,
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    shortcutResources: ShortcutResources,
    showTitle: Boolean = true,
    initialTitle: String = "",
    initialSelectedItems: List<Shortcut> = emptyList(),
    isEdit: Boolean = false
) {
    val context = LocalContext.current
    val loader = remember(initialSelectedItems) {
        CompositeChooserLoader(
            loaders = listOf(
                AllAppsIntentChooserLoader(context),
                ShortcutsChooserLoader(context, initialSelectedItems)
            )
        )
    }
    var title by remember { mutableStateOf("") }
    var titleManuallyChanged by remember { mutableStateOf(initialTitle.isNotEmpty()) }
    var selected by remember(initialSelectedItems) { mutableStateOf(initialSelectedItems.mapNotNull { it.intent.component }.toSet()) }
    var categoryFilter by remember { mutableStateOf(if (isEdit) CategoryFilterState.Selected else CategoryFilterState.All ) }
    var searchQuery by remember { mutableStateOf("") }
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
        asyncImage = { entry, colorFilter -> ChooserAsyncImage(entry, colorFilter, imageLoader) },
        emptyState = { filterApplied -> ChooserEmptyState(filterApplied) },
        topContent = { apps ->
            val (categoryNames, orderedCategoryIds) = categoryNamesAndIds(context, apps)
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                if (showTitle) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = title,
                        onValueChange = {
                            title = it
                            titleManuallyChanged = true
                        },
                        label = { Text(stringResource(id = R.string.title)) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                CategoryFilterChips(
                    categoryNames = categoryNames,
                    orderedCategoryIds = orderedCategoryIds,
                    categoryFilter = categoryFilter,
                    showSelectedCategory = isEdit,
                    onCategorySelected = { cat ->
                        categoryFilter = cat
                        if (showTitle && cat is CategoryFilterState.Category && !titleManuallyChanged) {
                            val idx = orderedCategoryIds.indexOf(cat.categoryId)
                            if (idx >= 0) {
                                title = categoryNames[idx]
                            }
                        }
                    },
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(id = R.string.search)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        },
        bottomContent = { apps ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) { Text(stringResource(id = android.R.string.cancel)) }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val selectedEntries = apps
                            .filter { it.componentName != null && selected.contains(it.componentName) }
                            .map {
                                it.isAppEntry = true
                                it.toShortcutIntent()
                            }
                        val folderIntent = Intent().forFolder(
                            title = title,
                            ctx = context,
                            target = shortcutResources
                        )
                        onSave(folderIntent, selectedEntries)
                    },
                    enabled = selected.isNotEmpty()
                ) { Text(stringResource(id = if (isEdit) R.string.save else R.string.create)) }
            }
        },
        listFilter = { list ->
            val preFiltered = when (categoryFilter) {
                is CategoryFilterState.Selected -> list.filter { it.componentName?.let { cn -> selected.contains(cn) } == true || it.sourceLoader == 1 }
                is CategoryFilterState.Category -> list.filter { it.category == (categoryFilter as CategoryFilterState.Category).categoryId  && it.sourceLoader == 0  }
                else -> list.filter { it.sourceLoader == 0 }
            }
            val q = searchQuery.trim()
            if (q.isEmpty()) preFiltered else preFiltered.filter { it.title.contains(q, ignoreCase = true) }
        }
    )
}

sealed interface CategoryFilterState {
    data object All : CategoryFilterState
    data class Category(val categoryId: Int) : CategoryFilterState
    data object Selected : CategoryFilterState
}

@Composable
private fun CategoryFilterChips(
    categoryNames: List<String>,
    orderedCategoryIds: List<Int>,
    categoryFilter: CategoryFilterState,
    showSelectedCategory: Boolean,
    onCategorySelected: (CategoryFilterState) -> Unit,
) {
    if (categoryNames.isNotEmpty()) {
        val allText = stringResource(id = R.string.all)
        val selectedText = stringResource(id = R.string.selected_items)
        val options = buildList {
            if (showSelectedCategory) add(selectedText)
            add(allText)
            addAll(categoryNames)
        }.toList() // keep order
        val selectedIndex = when {
            categoryFilter is CategoryFilterState.All && !showSelectedCategory -> 0
            categoryFilter is CategoryFilterState.All && showSelectedCategory -> 1
            categoryFilter is CategoryFilterState.Selected -> 0
            categoryFilter is CategoryFilterState.Category -> {
                val baseIndex = 1 + if (showSelectedCategory) 1 else 0
                val catPos = orderedCategoryIds.indexOf(categoryFilter.categoryId)
                if (catPos >= 0) baseIndex + catPos else 0
            }
            else -> 0
        }
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, text ->
                val selected = (index == selectedIndex)
                FilterChip(
                    selected = selected,
                    enabled = true,
                    modifier = Modifier.height(32.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    ),
                    colors = FilterChipDefaults.filterChipColors(),
                    onClick = {
                        val newState = when {
                            index == 0 && !showSelectedCategory -> CategoryFilterState.All
                            index == 0 && showSelectedCategory -> CategoryFilterState.Selected
                            index == 1 && showSelectedCategory -> CategoryFilterState.All
                            else -> {
                                val baseIndex = 1 + if (showSelectedCategory) 1 else 0
                                val catIdx = index - baseIndex
                                val categoryId = orderedCategoryIds.getOrNull(catIdx) ?: 0
                                CategoryFilterState.Category(categoryId)
                            }
                        }
                        onCategorySelected(newState)
                    },
                    label = { Text(text = text, style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun categoryNamesAndIds(context: Context, apps: List<ChooserEntry>): Pair<List<String>, List<Int>> {
    val presentIds = apps.map { it.category }.distinct().toSet()
    val ordered = listOf(
        ApplicationInfo.CATEGORY_GAME to R.string.game,
        ApplicationInfo.CATEGORY_AUDIO to R.string.audio,
        ApplicationInfo.CATEGORY_VIDEO to R.string.video,
        ApplicationInfo.CATEGORY_IMAGE to R.string.image,
        ApplicationInfo.CATEGORY_SOCIAL to R.string.social,
        ApplicationInfo.CATEGORY_NEWS to R.string.news,
        ApplicationInfo.CATEGORY_MAPS to R.string.navigation,
        ApplicationInfo.CATEGORY_PRODUCTIVITY to R.string.productivity,
    )
    val filtered = ordered.filter { presentIds.contains(it.first) }
    if (filtered.isEmpty()) return emptyList<String>() to emptyList()
    val names = filtered.map { (_, resId) -> context.getString(resId) }
    val ids = filtered.map { it.first }
    return names to ids
}

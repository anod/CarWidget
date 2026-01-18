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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import info.anodsplace.applog.AppLog
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
import info.anodsplace.compose.chooser.ChooserSelectedComponents
import info.anodsplace.compose.chooser.CompositeChooserLoader
import info.anodsplace.compose.chooser.MultiSelectChooserDialog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

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
    initialSelectedItems: ImmutableList<Shortcut> = persistentListOf(),
    isEdit: Boolean = false
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var titleManuallyChanged by remember { mutableStateOf(initialTitle.isNotEmpty()) }
    var selected by remember(initialSelectedItems) {
        mutableStateOf(
            ChooserSelectedComponents(
                initialSelectedItems.mapNotNull { it.intent.component }.toImmutableSet()
            )
        )
    }
    var categoryFilter by remember { mutableStateOf(if (isEdit) CategoryFilterState.Selected else CategoryFilterState.All) }
    var searchQuery by remember { mutableStateOf("") }

    val baseLoader = remember(initialSelectedItems) {
        CompositeChooserLoader(
            loaders = listOf(
                AllAppsIntentChooserLoader(context),
                ShortcutsChooserLoader(context, initialSelectedItems)
            )
        )
    }
    val appsFlow = remember(baseLoader) { baseLoader.load() }
    val appsList by appsFlow.collectAsState(initial = null)
    val (categoryNames, orderedCategoryIds) = remember (appsList) { categoryNamesAndIds(context, appsList) }
    val filteredList = remember(appsList, categoryFilter, searchQuery, selected) {
        val list = appsList ?: return@remember emptyList()
        val preFiltered = when (categoryFilter) {
            // include "shortcuts" loader items (sourceLoader == 1) in the Selected tab
            is CategoryFilterState.Selected -> list.filter {
                it.componentName?.let { cn -> selected.entries.contains(cn) } == true || it.sourceLoader == 1
            }
            // category filter applies only to apps loader (sourceLoader == 0)
            is CategoryFilterState.Category -> list.filter {
                it.category == (categoryFilter as CategoryFilterState.Category).categoryId && it.sourceLoader == 0
            }
            else -> list.filter { it.sourceLoader == 0 }
        }
        val q = searchQuery.trim()
        val result = if (q.isEmpty()) preFiltered else preFiltered.filter { it.title.contains(q, ignoreCase = true) }
        result
    }
    MultiSelectChooserDialog(
        list = filteredList.toImmutableList(),
        modifier = Modifier.padding(horizontal = 16.dp),
        headers = persistentListOf(),
        selectedComponents = selected,
        style = ChooserGridListDefaults.multiSelect().copy(grayscaleUnselectedIcons = true),
        onSelect = { entry ->
            AppLog.d("MultiSelectChooserDialog onSelect $entry")
            val component = entry.componentName ?: return@MultiSelectChooserDialog
            val entries = if (selected.entries.contains(component)) selected.entries - component else selected.entries + component
            selected = selected.copy(entries = entries.toImmutableSet())
            AppLog.d("MultiSelectChooserDialog onSelect $selected")
        },
        onDismissRequest = onDismissRequest,
        asyncImage = { entry, colorFilter -> ChooserAsyncImage(entry, colorFilter, imageLoader) },
        emptyState = { filterApplied -> ChooserEmptyState(filterApplied) },
        topContent = {
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
        bottomContent = { displayedList ->
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
                        val selectedEntries = displayedList
                            .filter { it.componentName != null && selected.entries.contains(it.componentName) }
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
                    enabled = selected.entries.isNotEmpty()
                ) { Text(stringResource(id = if (isEdit) R.string.save else R.string.create)) }
            }
        },
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
        val selectedIndex = when (categoryFilter) {
            is CategoryFilterState.All if !showSelectedCategory -> 0
            is CategoryFilterState.All if showSelectedCategory -> 1
            is CategoryFilterState.Selected -> 0
            is CategoryFilterState.Category -> {
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
                        val newState = when (index) {
                            0 if !showSelectedCategory -> CategoryFilterState.All
                            0 if showSelectedCategory -> CategoryFilterState.Selected
                            1 if showSelectedCategory -> CategoryFilterState.All
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

private fun categoryNamesAndIds(context: Context, apps: List<ChooserEntry>?): Pair<List<String>, List<Int>> {
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
    val presentIds = apps?.map { it.category }?.distinct()?.toSet() ?: ordered.map { it.first }
    val filtered = ordered.filter { presentIds.contains(it.first) }
    if (filtered.isEmpty()) return emptyList<String>() to emptyList()
    val names = filtered.map { (_, resId) -> context.getString(resId) }
    val ids = filtered.map { it.first }
    return names to ids
}
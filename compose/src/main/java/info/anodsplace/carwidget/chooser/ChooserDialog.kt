package info.anodsplace.carwidget.chooser

import android.content.ComponentName
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.compose.SystemIconShape

private val iconSize = 56.dp

@Immutable
data class ChooserGridListStyle(
    val grayscaleUnselectedIcons: Boolean,
    val dimUnselectedIcons: Boolean,
    val dimAlpha: Float,
    val showSelectionOutline: Boolean,
    val selectionOutlineColor: Color,
    val selectionOutlineWidth: Dp,
    val animateSelection: Boolean,
)

object ChooserGridListDefaults {
    @Composable
    fun style(
        grayscaleUnselectedIcons: Boolean = false,
        dimUnselectedIcons: Boolean = true,
        dimAlpha: Float = 0.35f,
        showSelectionOutline: Boolean = true,
        selectionOutlineColor: Color = MaterialTheme.colorScheme.primary,
        selectionOutlineWidth: Dp = 2.dp,
        animateSelection: Boolean = true,
    ): ChooserGridListStyle {
        val safeDimAlpha = dimAlpha.coerceIn(0f, 1f)
        return ChooserGridListStyle(
            grayscaleUnselectedIcons = grayscaleUnselectedIcons,
            dimUnselectedIcons = dimUnselectedIcons,
            dimAlpha = safeDimAlpha,
            showSelectionOutline = showSelectionOutline,
            selectionOutlineColor = selectionOutlineColor,
            selectionOutlineWidth = selectionOutlineWidth,
            animateSelection = animateSelection
        )
    }

    @Composable
    fun singleSelect(): ChooserGridListStyle = style(
        grayscaleUnselectedIcons = false,
        dimUnselectedIcons = false,
        showSelectionOutline = false
    )

    @Composable
    fun multiSelect(): ChooserGridListStyle = style()
}

@Composable
private fun EntryItem(
    entry: ChooserEntry,
    icon: @Composable (saturation: Float) -> Unit,
    onClick: (ChooserEntry) -> Unit = { },
    isSelected: Boolean = true,
    style: ChooserGridListStyle = ChooserGridListDefaults.style(),
) {
    // Apply visual effects purely based on style + selection state
    val applyGrayscale = !isSelected && style.grayscaleUnselectedIcons
    val targetAlpha = if (!isSelected && style.dimUnselectedIcons) style.dimAlpha else 1f
    val animatedAlpha = if (style.animateSelection) animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(250), label = "alpha").value else targetAlpha
    val targetSaturation = if (applyGrayscale) 0f else 1f
    val animatedSaturation = if (style.animateSelection) animateFloatAsState(targetValue = targetSaturation, animationSpec = tween(300), label = "saturation").value else targetSaturation
    val outlineTargetColor = if (isSelected && style.showSelectionOutline) style.selectionOutlineColor else Color.Transparent
    val outlineColor = if (style.animateSelection) animateColorAsState(targetValue = outlineTargetColor, animationSpec = tween(250), label = "outline-color").value else outlineTargetColor

    Column(
        modifier = Modifier
            .then(if (isSelected && style.showSelectionOutline) Modifier.border(style.selectionOutlineWidth, outlineColor, MaterialTheme.shapes.medium) else Modifier)
            .clip(shape = MaterialTheme.shapes.medium)
            .semantics { this.selected = isSelected }
            .clickable { onClick(entry) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.alpha(animatedAlpha)) { icon(animatedSaturation) }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = entry.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            // Accessibility: keep full opacity for unselected items so text remains readable
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChooserGridList(
    headers: List<ChooserEntry>,
    list: List<ChooserEntry>,
    imageLoader: ImageLoader,
    headerShape: Shape = MaterialTheme.shapes.medium,
    selectedComponents: Set<ComponentName> = emptySet(),
    onSelect: (ChooserEntry) -> Unit = { },
    style: ChooserGridListStyle = ChooserGridListDefaults.style(),
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Adaptive(64.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = rememberLazyGridState()
    ) {
        items(headers.size) { index ->
            val entry = headers[index] as Header
            EntryItem(
                entry,
                onClick = { onSelect(entry) },
                icon = { _ ->
                    if (entry.iconVector != null) {
                        Icon(
                            imageVector = entry.iconVector,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .size(iconSize)
                                .background(MaterialTheme.colorScheme.primary, shape = headerShape)
                                .padding(2.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        AsyncImage(
                            model = LocalContext.current.iconUri(iconRes = entry.iconRes),
                            contentDescription = entry.title,
                            imageLoader = imageLoader,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                },
                isSelected = true,
                style = style
            )
        }
        items(list.size) { index ->
            val entry = list[index]
            val component = entry.componentName
            val isSelected = component != null && selectedComponents.contains(component)
            EntryItem(
                entry,
                onClick = { onSelect(entry) },
                icon = { saturation ->
                    val colorMatrix = remember(saturation) {
                        ColorMatrix().apply { setToSaturation(saturation) }
                    }
                    AsyncImage(
                        model = entry.iconUri(LocalContext.current),
                        contentDescription = entry.title,
                        imageLoader = imageLoader,
                        modifier = Modifier.size(iconSize),
                        colorFilter = ColorFilter.colorMatrix(colorMatrix)
                    )
                },
                isSelected = isSelected,
                style = style
            )
        }
    }
}

@Composable
fun ChooserDialog(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = listOf(),
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    onClick: (ChooserEntry) -> Unit,
    style: ChooserGridListStyle = ChooserGridListDefaults.singleSelect(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ChooserScreen(
            loader = loader,
            modifier = modifier,
            headers = headers,
            imageLoader = imageLoader,
            selectedComponents = emptySet(),
            onSelect = onClick,
            style = style
        )
    }
}

@Composable
fun ChooserScreen(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = listOf(),
    imageLoader: ImageLoader,
    selectedComponents: Set<ComponentName> = emptySet(),
    onSelect: (ChooserEntry) -> Unit = { },
    style: ChooserGridListStyle = ChooserGridListDefaults.singleSelect(),
) {
    val appsList by loader.load().collectAsState(initial = emptyList())
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }
    val headerShape = SystemIconShape(iconSizePx)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 352.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        ChooserGridList(
            headers = headers,
            list = appsList,
            imageLoader = imageLoader,
            headerShape = headerShape,
            selectedComponents = selectedComponents,
            onSelect = onSelect,
            style = style
        )
    }
}

@Composable
fun MultiSelectChooserDialog(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = emptyList(),
    selectedComponents: Set<ComponentName> = emptySet(),
    onSelect: (ChooserEntry) -> Unit = { },
    onDismissRequest: () -> Unit,
    imageLoader: ImageLoader,
    minHeight: Dp = 420.dp,
    // Provide original (unfiltered) list of apps to top content so it can build filters (chips)
    topContent: @Composable (List<ChooserEntry>) -> Unit = {},
    bottomContent: @Composable (List<ChooserEntry>) -> Unit = {},
    style: ChooserGridListStyle = ChooserGridListDefaults.multiSelect(),
    // List transformation applied before displaying in grid (selection still works with original components)
    listFilter: (List<ChooserEntry>) -> List<ChooserEntry> = { it }
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MultiSelectChooserContent(
            loader = loader,
            modifier = modifier,
            headers = headers,
            selectedComponents = selectedComponents,
            onSelect = onSelect,
            imageLoader = imageLoader,
            minHeight = minHeight,
            topContent = topContent,
            bottomContent = bottomContent,
            style = style,
            listFilter = listFilter
        )
    }
}

@Composable
private fun MultiSelectChooserContent(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = emptyList(),
    selectedComponents: Set<ComponentName> = emptySet(),
    onSelect: (ChooserEntry) -> Unit = { },
    imageLoader: ImageLoader,
    minHeight: Dp = 420.dp,
    topContent: @Composable (List<ChooserEntry>) -> Unit = {},
    bottomContent: @Composable (List<ChooserEntry>) -> Unit = {},
    style: ChooserGridListStyle = ChooserGridListDefaults.multiSelect(),
    listFilter: (List<ChooserEntry>) -> List<ChooserEntry> = { it }
) {
    val appsList by loader.load().collectAsState(initial = emptyList())
    val filteredList = remember(appsList, listFilter) { listFilter(appsList) }
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }
    val headerShape = SystemIconShape(iconSizePx)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column {
            topContent(appsList)
            Box(modifier = Modifier.weight(1f, fill = true)) {
                ChooserGridList(
                    headers = headers,
                    list = filteredList,
                    imageLoader = imageLoader,
                    headerShape = headerShape,
                    selectedComponents = selectedComponents,
                    onSelect = onSelect,
                    style = style
                )
            }
            // Pass original unfiltered list to bottomContent so selection works even if item hidden by filter
            bottomContent(appsList)
        }
    }
}

@Preview(name = "Chooser Screen", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ChooserScreenPreview() {
    CarWidgetTheme {
        ChooserScreen(
            loader = StaticChooserLoader(listOf(
                ChooserEntry(componentName = null, title = "Music", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play_primary),
                ChooserEntry(componentName = null, title = "Maps", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play_primary),
                ChooserEntry(componentName = null, title = "Calls", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play_primary)
            )),
            headers = listOf(
                Header(0, "Actions", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play_primary),
                Header(0, "More", Icons.Filled.Alarm)
            ),
            imageLoader = ImageLoader(LocalContext.current),
            style = ChooserGridListDefaults.multiSelect().copy(
                grayscaleUnselectedIcons = true,
                dimUnselectedIcons = true,
                dimAlpha = 0.4f,
                showSelectionOutline = true
            )
        )
    }
}

@Preview(name = "MultiSelect Content", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MultiSelectChooserContentPreview() {
    CarWidgetTheme {
        val ctx = LocalContext.current
        val app1 = ChooserEntry(componentName = ComponentName("pkg.sample", "pkg.sample.App1"), title = "App One")
        val app2 = ChooserEntry(componentName = ComponentName("pkg.sample", "pkg.sample.App2"), title = "App Two")
        val app3 = ChooserEntry(componentName = ComponentName("pkg.sample", "pkg.sample.App3"), title = "App Three")
        MultiSelectChooserDialog(
            loader = StaticChooserLoader(listOf(app1, app2, app3)),
            headers = listOf(Header(0, "Actions", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play_primary)),
            selectedComponents = setOf(app2.componentName!!),
            onSelect = { },
            onDismissRequest = { },
            imageLoader = ImageLoader(ctx),
            style = ChooserGridListDefaults.multiSelect().copy(
                grayscaleUnselectedIcons = true,
                dimUnselectedIcons = true,
                dimAlpha = 0.4f,
                selectionOutlineColor = MaterialTheme.colorScheme.secondary
            ),
            topContent = { all -> Text("Select apps (${all.size})", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium) },
            bottomContent = { list -> Text("${list.size} apps", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
        )
    }
}

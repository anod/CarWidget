package info.anodsplace.carwidget.chooser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.compose.SystemIconShape

private val iconSize = 56.dp

@Composable
private fun EntryItem(entry: ChooserEntry, icon: @Composable () -> Unit, onClick: (ChooserEntry) -> Unit = { }) {
    Column(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable { onClick(entry) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = entry.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun ChooserGridList(
    headers: List<ChooserEntry>,
    list: List<ChooserEntry>,
    onClick: (ChooserEntry) -> Unit,
    imageLoader: ImageLoader,
    headerShape: Shape = MaterialTheme.shapes.medium
) {
    val context = LocalContext.current
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
                onClick = onClick,
                icon = {
                    if (entry.iconVector != null) {
                        Icon(
                            imageVector = entry.iconVector,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .size(iconSize)
                                .background(MaterialTheme.colorScheme.primary, shape = headerShape)
                            ,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        AsyncImage(
                            model = context.iconUri(iconRes = entry.iconRes),
                            contentDescription = entry.title,
                            imageLoader = imageLoader,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            )
        }
        items(list.size) { index ->
            val entry = list[index]
            EntryItem(
                entry,
                onClick = onClick,
                icon = {
                    AsyncImage(
                        model = entry.iconUri(context),
                        contentDescription = entry.title,
                        imageLoader = imageLoader,
                        modifier = Modifier.size(iconSize)
                    )
                }
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
    onClick: (ChooserEntry) -> Unit,
    imageLoader: ImageLoader
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ChooserScreen(
            loader,
            modifier,
            headers,
            onClick,
            imageLoader
        )
    }
}

@Composable
fun ChooserScreen(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = listOf(),
    onClick: (ChooserEntry) -> Unit,
    imageLoader: ImageLoader,

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
            headers,
            appsList,
            onClick = onClick,
            imageLoader = imageLoader,
            headerShape = headerShape
        )
    }
}

@Preview
@Composable
fun ChooserScreenPreview() {
    ChooserScreen(
        StaticChooserLoader(listOf(
            ChooserEntry(componentName = null, title = "Very long name title of entry"),
        )),
        headers = listOf(
            Header(0, "Show choice", iconRes = info.anodsplace.carwidget.skin.R.drawable.ic_shortcut_play),
            Header(0, "Very long name title of entry", Icons.Filled.Alarm)
        ),
        onClick = {  },
        imageLoader = ImageLoader(LocalContext.current)
    )
}
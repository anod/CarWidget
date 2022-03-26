package info.anodsplace.carwidget.chooser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.anodsplace.compose.PicassoIcon
import info.anodsplace.carwidget.utils.SystemIconSize

@Composable
fun EntryIcon(entry: ChooserEntry, onClick: (ChooserEntry) -> Unit) {
    val iconModifier = Modifier
        .size(SystemIconSize)
        .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(48.dp))
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable { onClick(entry) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (entry is Header) {
            Icon(
                imageVector = entry.iconVector,
                contentDescription = entry.title,
                modifier = iconModifier,
                tint = Color.Unspecified
            )
        } else {
            PicassoIcon(
                uri = entry.iconUri(context),
                contentDescription = entry.title,
                modifier = iconModifier
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = entry.title,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun ChooserGridList(headers: List<ChooserEntry>, list: List<ChooserEntry>, onClick: (ChooserEntry) -> Unit) {

    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        columns = GridCells.Adaptive(64.dp),
        state = rememberLazyGridState()
    ) {
        items(headers.size) { index ->
            val entry = headers[index]
            EntryIcon(entry, onClick)
        }
        items(list.size) { index ->
            val entry = list[index]
            EntryIcon(entry, onClick)
        }
    }
}

@Composable
fun ChooserDialog(
    loader: ChooserLoader,
    modifier: Modifier = Modifier,
    headers: List<ChooserEntry> = listOf(),
    onDismissRequest: () -> Unit,
    onClick: (ChooserEntry) -> Unit
) {
    val appsList by loader.load().collectAsState(initial = emptyList())
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 352.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface
        ) {
            ChooserGridList(headers, appsList, onClick = onClick)
        }
    }
}
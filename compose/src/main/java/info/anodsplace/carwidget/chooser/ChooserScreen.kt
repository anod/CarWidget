package info.anodsplace.carwidget.chooser

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.ImageLoader
import coil.compose.AsyncImage
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.content.iconUri
import info.anodsplace.compose.chooser.ChooserEntry
import info.anodsplace.compose.chooser.chooserIconSize

@Composable
fun ChooserAsyncImage(entry: ChooserEntry, colorFilter: ColorFilter?, imageLoader: ImageLoader): Unit {
    val fallbackPainter = rememberVectorPainter(Icons.Filled.Android)
    AsyncImage(
        model = LocalContext.current.iconUri(iconRes = entry.iconRes),
        contentDescription = entry.title,
        imageLoader = imageLoader,
        modifier = Modifier.size(chooserIconSize),
        colorFilter = colorFilter,
        error = fallbackPainter,
        fallback = fallbackPainter
    )
}

@Composable
fun ChooserEmptyState(filterApplied: Boolean) {
    val message = if (filterApplied) stringResource(id = R.string.chooser_empty_filtered_list) else
        stringResource(id = R.string.chooser_empty_list)
    info.anodsplace.compose.chooser.ChooserEmptyState(message = message, modifier = Modifier)
}
package info.anodsplace.carwidget.iconpack

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IconPackScreen(onSelect: (Bitmap, resId: Int) -> Unit, initialFolderMode: Boolean = false) {
    val scope = rememberCoroutineScope()
    var folderMode by rememberSaveable { mutableStateOf(initialFolderMode) }
    Scaffold(
        topBar = { TopAppBar(
            title = { Text(text = stringResource(id = R.string.icon_pack_label)) },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = stringResource(id = R.string.icon_pack_toggle_show_folders), style = MaterialTheme.typography.labelSmall)
                    Switch(checked = folderMode, onCheckedChange = { folderMode = it })
                }
            }
        ) }
    ) { inner ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(IconDescriptions) { iconDesc ->
                val captureController = rememberCaptureController()
                val resId = if (folderMode) iconDesc.folderIconRes else iconDesc.iconRes
                Card(
                    modifier = Modifier
                        .then(Modifier),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = {
                        scope.launch {
                            val bitmapAsync = captureController.captureAsync()
                            try {
                                val image = bitmapAsync.await()
                                onSelect(image.asAndroidBitmap(), resId)
                            } catch (_: Throwable) {
                            }
                        }
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize() // Fill the fixed card height so background is uniform
                    ) {
                        val context = LocalContext.current
                        val drawable = remember(resId) { context.getDrawable(resId) }
                        if (drawable != null) {
                            Icon(
                                painter = rememberDrawablePainter(drawable = drawable),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                                    .capturable(captureController)
                            )
                        }
                        Text(
                            text = stringResource(id = iconDesc.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .align(Alignment.Start)
                        )
                    }
                }
            }
        }
    }
}


@Preview(name = "IconPack Light", showBackground = true)
@Composable
private fun IconPackScreenPreviewLight() {
    MaterialTheme { IconPackScreen(onSelect = { _, _ -> }) }
}

@Preview(name = "IconPack Folders Light", showBackground = true)
@Composable
private fun IconPackScreenPreviewFolders() {
    MaterialTheme { IconPackScreen(onSelect = { _, _ -> }, initialFolderMode = true) }
}
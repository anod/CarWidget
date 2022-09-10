package info.anodsplace.carwidget.screens.incar

import android.app.UiModeManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.ChooserLoader
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.StaticChooserLoader
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize

@Composable
fun NotificationShortcuts(
    screenState: InCarViewState,
    modifier: Modifier = Modifier,
    onEvent: (InCarViewEvent) -> Unit = { },
    imageLoader: ImageLoader,
    appsLoader: ChooserLoader = StaticChooserLoader(emptyList()),
) {
    val context = LocalContext.current
    var shortcutIndex: Int by remember { mutableStateOf(-1) }
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(4f)
        ) {
            Text(
                text = stringResource(id = R.string.shortcuts),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.shortcuts_summary),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        val shortcutsModel = screenState.notificationShortcuts
        if (shortcutsModel != null) {
            for (i in 0 until shortcutsModel.count()) {
                val shortcut = shortcutsModel.get(i)
                val iconModifier = Modifier
                    .size(SystemIconSize)
                    .padding(4.dp)
                if (shortcut == null) {
                    Icon(
                            modifier = iconModifier
                                .weight(1f)
                                .clickable(onClick = { shortcutIndex = i }),
                            imageVector = Icons.Filled.Add,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null
                    )
                } else {
                    AsyncImage(
                        model = shortcut.iconUri(context, ""),
                        contentDescription = shortcut.title.toString(),
                        imageLoader = imageLoader,
                        modifier = iconModifier
                            .weight(1f)
                            .clickable(onClick = { shortcutIndex = i })
                    )
                }
            }
        }

        if (shortcutIndex >= 0) {
            ChooserDialog(
                modifier = Modifier.fillMaxHeight(fraction = 0.8f),
                headers = listOf(
                    Header(0, stringResource(R.string.none), iconVector = Icons.Filled.Cancel)
                ),
                loader = appsLoader,
                onDismissRequest = { shortcutIndex = -1 },
                onClick = { entry ->
                    onEvent(InCarViewEvent.NotificationShortcutUpdate(shortcutIndex, entry))
                    shortcutIndex = -1
                },
                imageLoader = imageLoader
            )
        }
    }
}

@Preview("ShortcutsScreen Dark")
@Composable
fun ShortcutsScreenDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        Surface {
            NotificationShortcuts(
                screenState = InCarViewState(items = emptyList()),
                imageLoader = ImageLoader.Builder(LocalContext.current).build()
            )
        }
    }
}
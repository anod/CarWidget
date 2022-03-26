package info.anodsplace.carwidget.screens.incar

import android.app.UiModeManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.content.graphics.imageLoader
import info.anodsplace.carwidget.utils.SystemIconSize
import info.anodsplace.compose.BackgroundSurface
import kotlinx.coroutines.launch

@Composable
fun NotificationShortcuts(viewModel: InCarViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var shortcutIndex: Int by remember { mutableStateOf(-1) }
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(4f)
        ) {
            Text(
                text = stringResource(id = R.string.shortcuts),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body2
            )
            Text(
                text = stringResource(id = R.string.shortcuts_summary),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body2
            )
        }
        val shortcutsModel by viewModel.notificationShortcuts.collectAsState(initial = null)
        if (shortcutsModel != null) {
            for (i in 0 until shortcutsModel!!.count) {
                val shortcut = shortcutsModel!!.get(i)
                val iconModifier = Modifier
                    .size(SystemIconSize)
                    .padding(4.dp)
                if (shortcut == null) {
                    Icon(
                            modifier = iconModifier
                                .weight(1f)
                                .clickable(onClick = { shortcutIndex = i }),
                            imageVector = Icons.Filled.Add,
                            tint = MaterialTheme.colors.onSurface,
                            contentDescription = null
                    )
                } else {
                    AsyncImage(
                        model = shortcut.iconUri(context, ""),
                        contentDescription = shortcut.title.toString(),
                        imageLoader = context.imageLoader,
                        modifier = iconModifier
                            .weight(1f)
                            .clickable(onClick = { shortcutIndex = i })
                    )
                }
            }
        }

        val scope = rememberCoroutineScope()
        if (shortcutIndex >= 0) {
            ChooserDialog(
                modifier = Modifier.fillMaxHeight(fraction = 0.8f),
                headers = listOf(
                    Header(0, stringResource(R.string.none), iconVector = Icons.Filled.Cancel)
                ),
                loader = viewModel.appsLoader,
                onDismissRequest = { shortcutIndex = -1 },
                onClick = { entry ->
                    scope.launch {
                        if (entry.componentName == null) {
                            shortcutsModel?.drop(shortcutIndex)
                        } else {
                            shortcutsModel?.saveIntent(
                                    shortcutIndex,
                                    entry.getIntent(baseIntent = null),
                                    isApplicationShortcut = true
                            )
                        }
                        shortcutIndex = -1
                    }
                })
        }
    }
}

@Preview("ShortcutsScreen Dark")
@Composable
fun ShortcutsScreenDark() {
    CarWidgetTheme(nightMode = UiModeManager.MODE_NIGHT_YES) {
        BackgroundSurface {
            NotificationShortcuts(
                viewModel = viewModel(),
                modifier = Modifier
            )
        }
    }
}
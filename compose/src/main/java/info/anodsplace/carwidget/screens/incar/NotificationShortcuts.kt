package info.anodsplace.carwidget.screens.incar

import android.content.Intent
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
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.AppsPackageLoader
import info.anodsplace.carwidget.chooser.ChooserDialog
import info.anodsplace.carwidget.chooser.Header
import info.anodsplace.carwidget.chooser.MediaListLoader
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.screens.about.AboutUiAction
import info.anodsplace.carwidget.utils.SystemIconSize
import info.anodsplace.compose.PicassoIcon
import info.anodsplace.framework.content.forLauncher

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
        for (i in 0 until viewModel.notificationShortcuts.count) {
            val shortcut = viewModel.notificationShortcuts.get(i)
            val iconModifier = Modifier
                .size(SystemIconSize)
                .padding(4.dp)
            if (shortcut == null) {
                Icon(
                    modifier = iconModifier.weight(1f).clickable(onClick = { shortcutIndex = i }),
                    imageVector = Icons.Filled.Add,
                    tint = MaterialTheme.colors.onSurface,
                    contentDescription = null
                )
            } else {
                PicassoIcon(
                    modifier = iconModifier.weight(1f).clickable(onClick = { shortcutIndex = i }),
                    uri = shortcut.iconUri(context, ""),
                )
            }
        }

        if (shortcutIndex >= 0) {
            ChooserDialog(
                modifier = Modifier.fillMaxHeight(fraction = 0.8f),
                headers = listOf(
                    Header(stringResource(R.string.none), iconVector = Icons.Filled.Cancel)
                ),
                appsLoader = viewModel.appsLoader,
                onDismissRequest = { shortcutIndex = -1 },
                onClick = { entry ->
                    if (entry.componentName == null) {
                        viewModel.notificationShortcuts.drop(shortcutIndex)
                    } else {
                        viewModel.notificationShortcuts.saveIntent(
                                shortcutIndex,
                                entry.getIntent(baseIntent = null),
                                isApplicationShortcut = true
                        )
                    }
                    shortcutIndex = -1
                })
        }
    }
}

@Preview("ShortcutsScreen Dark")
@Composable
fun ShortcutsScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            NotificationShortcuts(
                viewModel = viewModel(),
                modifier = Modifier
            )
        }
    }
}
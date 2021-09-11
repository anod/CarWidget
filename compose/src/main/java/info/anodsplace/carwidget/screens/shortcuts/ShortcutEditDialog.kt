package info.anodsplace.carwidget.screens.shortcuts

import android.app.UiModeManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.utils.SystemMaxIconSize

@Composable
fun ShortcutEditDialog(cellId: Int, shortcut: Shortcut, icon: ShortcutIcon) {

    Scaffold(
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            topBar = {
                TopAppBar(
                        title = { Text(text = stringResource(id = R.string.shortcut_edit_title)) },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Rounded.Info, contentDescription = stringResource(id = R.string.info))
                            }
                        }
                )
            }
    ) { innerPadding ->
        Column(
                modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 144.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {}) {
                    Icon(
                            modifier = Modifier
                                    .size(SystemMaxIconSize)
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                                    .background(Color.Gray)
                                    .clickable(onClick = { }),
                            imageVector = Icons.Filled.Android,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = null
                    )
                }
                TextField(
                    label = { Text("Title") },
                    value = shortcut.title.toString(),
                    onValueChange = {},
                    singleLine = true
                )
            }
            Row {
                Button(onClick = { }, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(text = "Delete")
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(onClick = {  }, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(text = "OK")
                }
            }
        }
    }
}

@Preview("Shortcut edit dialog")
@Composable
fun PreviewEditDialog() {
    CarWidgetTheme(nightMode = UiModeManager.MODE_NIGHT_YES) {
        ShortcutEditDialog(0,
                Shortcut(
                    id = 0L,
                    itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION,
                    title = "App title",
                    isCustomIcon = false,
                    intent = Intent()
                ),
                ShortcutIcon.forCustomIcon(0L, Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
        )
    }
}
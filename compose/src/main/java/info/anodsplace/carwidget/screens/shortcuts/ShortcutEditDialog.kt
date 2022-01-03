package info.anodsplace.carwidget.screens.shortcuts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.chooser.ChooserGridList
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.utils.SystemIconSize
import info.anodsplace.carwidget.utils.SystemMaxIconSize
import info.anodsplace.compose.PicassoIcon

@Composable
fun ShortcutEditDialog(viewModel: ShortcutEditViewModel, onDismissRequest: () -> Unit) {
    Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties()
    ) {
        Surface(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 352.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface
        ) {
            val shortcut = viewModel.shortcut
            Column(
            ) {
                TopAppBar(
                        title = { Text(text = stringResource(id = R.string.shortcut_edit_title)) }
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 144.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {}) {
                        PicassoIcon(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(4.dp)
                                .weight(1f)
                                .clickable(onClick = { }),
                            uri = shortcut.iconUri(LocalContext.current, ""),
                        )
                    }
                    TextField(
                        modifier = Modifier.padding(vertical = 8.dp),
                        label = { Text("Title") },
                        value = shortcut.title.toString(),
                        onValueChange = {},
                        singleLine = true
                    )
                }
                Row(
                    modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(onClick = {
                        viewModel.drop()
                        onDismissRequest()
                    }, modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(text = "Delete")
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Button(onClick = {
                        onDismissRequest()
                    }, modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(text = "OK")
                    }
                }
            }
        }

    }
}
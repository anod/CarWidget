package info.anodsplace.carwidget.screens.shortcuts

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.iconUri
import info.anodsplace.carwidget.screens.shortcuts.intent.IntentEditScreen
import info.anodsplace.carwidget.screens.shortcuts.intent.UpdateField
import info.anodsplace.compose.BackgroundSurface
import info.anodsplace.compose.LocalPicasso
import info.anodsplace.compose.PicassoIcon

@Composable
fun ShortcutEditScreen(viewModel: ShortcutEditViewModel, onDismissRequest: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    BackgroundSurface(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 352.dp),
            shape = MaterialTheme.shapes.medium,
    ) {
        val shortcut by viewModel.shortcut.collectAsState(null)
        if (shortcut != null) {
            ShortcutEditContent(
                    shortcut = shortcut!!,
                    delegate = viewModel,
                    onDismissRequest = onDismissRequest,
                    expanded = expanded
            )
        }
    }
}

@Composable
fun ShortcutEditContent(shortcut: Shortcut, delegate: ShortcutEditDelegate, onDismissRequest: () -> Unit, expanded: MutableState<Boolean>) {
    Column(
    ) {
        TopAppBar {
            Text(text = stringResource(id = R.string.shortcut_edit_title))
        }
        if (!expanded.value) {
            ShortcutInfo(shortcut, delegate, onDismissRequest)
            AdvancedButton(expanded)
        } else {
            AdvancedButton(expanded)
            IntentEditScreen(
                    intent = shortcut.intent,
                    updateField = { delegate.updateField(it.field) },
                    modifier = Modifier
            )
        }
    }
}

@Composable
fun AdvancedButton(expanded: MutableState<Boolean>) {
    OutlinedButton(
            modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
            onClick = { expanded.value = !expanded.value }
    ) {
        Text(text = stringResource(id = R.string.advanced))
        if (expanded.value) {
            Icon(Icons.Filled.ArrowDropUp, contentDescription = "Expand")
        } else {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Collapse")
        }
    }
}

@Composable
fun ShortcutInfo(shortcut: Shortcut, delegate: ShortcutEditDelegate, onDismissRequest: () -> Unit) {
    Column (
            modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 144.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
            delegate.drop()
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


@Preview("Preview Shortcut Edit")
@Composable
fun PreviewShortcutEditContent() {
    CarWidgetTheme() {
        BackgroundSurface {
            CompositionLocalProvider(LocalPicasso provides Picasso.get()) {
                ShortcutEditContent(
                        shortcut = Shortcut(0, 0, "Title", false, Intent()),
                        delegate = ShortcutEditDelegate.NoOp(),
                        onDismissRequest = { },
                        expanded = mutableStateOf(true)
                )
            }
        }
    }
}
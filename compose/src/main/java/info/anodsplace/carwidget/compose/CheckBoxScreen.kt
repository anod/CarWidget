package info.anodsplace.carwidget.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.intent.IntentCategories
import info.anodsplace.carwidget.intent.IntentFlags
import androidx.compose.material.Text
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableSharedFlow

class CheckBoxScreenState(
        val title: String,
        val items: Map<String, Any?>,
        val checked: SnapshotStateList<String>
)

@Composable
fun CheckBoxList(items: Map<String, Any?>, checked: SnapshotStateList<String>, modifier: Modifier = Modifier, onCheckedChange: (key: String, value: Any?, checked: Boolean) -> Unit) {
    val checkedMap = checked.associateWith { true }
    rememberScrollState(0)
    LazyColumn(modifier = modifier) {
        items(items.entries.toList()) { item ->
            val isItemChecked = checkedMap.containsKey(item.key)
            val (itemChecked, onItemChecked) = remember { mutableStateOf(isItemChecked) }
            Row(modifier = Modifier
                            .toggleable(value = itemChecked, onValueChange = onItemChecked)
            ) {
                Checkbox(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = itemChecked,
                        onCheckedChange = { newState ->
                            if (checkedMap.containsKey(item.key)) {
                                if (!newState) {
                                    checked.remove(item.key)
                                }
                            } else {
                                if (newState) {
                                    checked.add(item.key)
                                }
                            }
                            onItemChecked(newState)
                        },
                )
                Text(
                        text = item.key,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CheckBoxScreen(state: CheckBoxScreenState, onDismissRequest: () -> Unit) {
    Surface(color = MaterialTheme.colors.background) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(modifier = Modifier.padding(16.dp).align(Alignment.Start)) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                    Text(state.title, style = MaterialTheme.typography.subtitle1)
                }
            }
            CheckBoxList(
                    items = state.items,
                    checked = state.checked,
                    modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(0.1f, fill = true),
            ) { _: String, _: Any?, _: Boolean -> }
            ButtonsPanel(onDismissRequest = onDismissRequest)
        }
    }
}

@Preview
@Composable
fun CheckBoxScreenPreview() {
    val state = CheckBoxScreenState(
        "Categories", IntentFlags, mutableStateListOf("ACTIVITY_NEW_TASK", "ACTIVITY_NEW_DOCUMENT")
    )
    CarWidgetTheme(darkTheme = false) {
        Scaffold(
                topBar = { CarWidgetToolbar(MutableSharedFlow()) },
                backgroundColor = MaterialTheme.colors.background,
                content = {
                    Box(contentAlignment = Alignment.Center) {
                        CheckBoxScreen(state, onDismissRequest = {})
                    }
                }
        )
    }
}

@Preview
@Composable
fun CheckBoxScreenPreviewDark() {
    val visibility = CheckBoxScreenState(
        "Flags", IntentCategories, mutableStateListOf("DEFAULT")
    )
    CarWidgetTheme(darkTheme = true) {
        Scaffold(
                topBar = { CarWidgetToolbar(MutableSharedFlow()) },
                backgroundColor = MaterialTheme.colors.background,
                content = {
                    Box(contentAlignment = Alignment.Center) {
                        CheckBoxScreen(visibility, onDismissRequest = {})
                    }
                }
        )
    }
}

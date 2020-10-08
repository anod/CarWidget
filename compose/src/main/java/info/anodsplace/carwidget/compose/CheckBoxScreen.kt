package info.anodsplace.carwidget.compose

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.prefs.IntentCategories
import info.anodsplace.carwidget.prefs.IntentFlags
import info.anodsplace.framework.livedata.SingleLiveEvent

sealed class CheckBoxScreenVisibility(val isVisible: Boolean) {
    object Hidden : CheckBoxScreenVisibility(false)
    class Visible(val title: String, val items: Map<String, Any?>, val checked: SnapshotStateList<String>) : CheckBoxScreenVisibility(true)
}

@Composable
fun rememberCheckBoxScreenState(initialVisibility: CheckBoxScreenVisibility = CheckBoxScreenVisibility.Hidden): MutableState<CheckBoxScreenVisibility> = remember { mutableStateOf(initialVisibility) }

@Composable
fun CheckBoxList(items: Map<String, Any?>, checked: SnapshotStateList<String>, modifier: Modifier = Modifier, onCheckedChange: (key: String, value: Any?, checked: Boolean) -> Unit) {
    val checkedMap = checked.associateWith { true }
    ScrollableColumn(modifier = modifier.padding(16.dp)) {
        for (item in items) {
            val isItemChecked = checkedMap.containsKey(item.key)
            val (itemChecked, onItemChecked) = remember { mutableStateOf(isItemChecked) }
            Row(
                    modifier = Modifier
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
                        checkedColor = MaterialTheme.colors.onSurface.copy(0.6f)
                )
                Text(
                        text = item.key,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.preferredHeight(8.dp))
            }
        }
    }
}

@Composable
fun CheckBoxScreen(state: MutableState<CheckBoxScreenVisibility>) {
    when (val sateValue = state.value) {
        is CheckBoxScreenVisibility.Visible -> CheckBoxList(sateValue.items, sateValue.checked) { _: String, _: Any?, _: Boolean -> }
        else -> {
        }
    }
}

@Composable
fun DialogButtons(onDismissRequest: () -> Unit) {
    Column {
        Divider()
        Row(
                Modifier
                        .preferredHeight(48.dp)
                        .fillMaxWidth()
                        .padding(8.dp)
        ) {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.save))
            }
        }
        Spacer(modifier = Modifier.preferredHeight(8.dp))
    }
}

@Composable
fun DialogContent(stateValue: CheckBoxScreenVisibility.Visible, onDismissRequest: () -> Unit) {
    Surface(color = Color.Transparent) {
        Column {
            CheckBoxList(
                    items = stateValue.items,
                    checked = stateValue.checked,
                    modifier = Modifier.weight(0.8f, fill = false),
            ) { _: String, _: Any?, _: Boolean -> }
            DialogButtons(onDismissRequest = onDismissRequest)
            //Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@OptIn(ExperimentalLayout::class)
@Composable
fun CheckBoxDialog(state: MutableState<CheckBoxScreenVisibility>, onDismissRequest: () -> Unit) {
    when (val stateValue = state.value) {
        is CheckBoxScreenVisibility.Visible -> {
            AlertDialog(
                    onDismissRequest = onDismissRequest,
                    title = { Text(text = stateValue.title) },
                    buttons = { DialogContent(stateValue, onDismissRequest = onDismissRequest) }
            )
        }
        else -> {
        }
    }
}

@Preview
@Composable
fun CheckBoxScreenPreview() {
    val visibility = CheckBoxScreenVisibility.Visible(
        "Categories", IntentFlags, mutableStateListOf("ACTIVITY_NEW_TASK", "ACTIVITY_NEW_DOCUMENT")
    )
    CarWidgetTheme(darkTheme = false) {
        Scaffold(
                topBar = { CarWidgetToolbar(SingleLiveEvent()) },
                backgroundColor = MaterialTheme.colors.background,
                bodyContent = {
                    Box(
                            alignment = Alignment.Center
                    ) {
                        DialogContent(visibility, onDismissRequest = {})
                    }
                }
        )
    }
}

@Preview
@Composable
fun CheckBoxScreenPreviewDark() {
    val visibility = CheckBoxScreenVisibility.Visible(
        "Flags", IntentCategories, mutableStateListOf("DEFAULT")
    )
    CarWidgetTheme(darkTheme = true) {
        Scaffold(
                topBar = { CarWidgetToolbar(SingleLiveEvent()) },
                backgroundColor = MaterialTheme.colors.background,
                bodyContent = {
                    Box(
                            alignment = Alignment.Center
                    ) {
                        DialogContent(visibility, onDismissRequest = {})
                    }
                }
        )
    }
}

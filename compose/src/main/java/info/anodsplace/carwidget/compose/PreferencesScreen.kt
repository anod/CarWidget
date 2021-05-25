package info.anodsplace.carwidget.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategory(item: PreferenceItem.Category, modifier: Modifier = Modifier) {
    Text(
        text = if (item.titleRes != 0) stringResource(id = item.titleRes) else item.title,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.caption.copy(
            color = MaterialTheme.colors.primary
        )
    )
}

@Composable
fun Preference(item: PreferenceItem, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = modifier.weight(8f)
        ) {
            Text(
                text = if (item.titleRes != 0) stringResource(id = item.titleRes) else item.title,
                style = MaterialTheme.typography.subtitle2
            )
            if (item.summaryRes != 0 || item.summary.isNotEmpty()) {
                Text(
                    text = if (item.summaryRes != 0) stringResource(id = item.summaryRes) else item.summary,
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.secondary
                    )
                )
            }
        }
        Box(
            modifier = modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PreferenceSwitch(checked: Boolean, item: PreferenceItem, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
    Preference(item, modifier, onClick = {
        onCheckedChange(!checked)
    }) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors()
        )
    }
}

@Composable
fun PreferenceCheckbox(checked: Boolean, item: PreferenceItem, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
    Preference(item, modifier, onClick = {
        onCheckedChange(!checked)
    }) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                disabledColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
            )
        )
    }
}

@Composable
fun PreferencesScreen(preferences: List<PreferenceItem>, onClick: (item: PreferenceItem) -> Unit, modifier: Modifier = Modifier) {
    var listItem by remember { mutableStateOf<PreferenceItem.List?>(null) }
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(preferences.size) { index ->
            when (val item = preferences[index]) {
                is PreferenceItem.Category -> PreferenceCategory(item = item)
                is PreferenceItem.CheckBox -> {
                    var checked by remember { mutableStateOf(item.checked) }
                    PreferenceCheckbox(checked, item, onCheckedChange = { newChecked ->
                        checked = newChecked
                        item.checked = newChecked
                        onClick(item)
                    })
                }
                is PreferenceItem.List -> Preference(item, onClick = { listItem = item }) { }
                is PreferenceItem.Switch -> {
                    var checked by remember { mutableStateOf(item.checked) }
                    PreferenceSwitch(checked, item, onCheckedChange = { newChecked ->
                        item.checked = newChecked
                        checked = newChecked
                        onClick(item)
                    })
                }
                is PreferenceItem.Text -> Preference(item, onClick = { onClick(item) }) { }
            }
        }
    }

    if (listItem != null) {
        PreferenceListDialog(item = listItem!!) { value ->
            onClick(listItem!!)
            listItem = null
        }
    }
}

@Composable
fun PreferenceListDialog(item: PreferenceItem.List, onValueChange: (value: String) -> Unit) {
    val entries = stringArrayResource(id = item.entries)
    val entryValues = stringArrayResource(id = item.entryValues)
    var value by remember { mutableStateOf(item.value) }
    AlertDialog(
        title = { Text(text = if (item.titleRes != 0) stringResource(id = item.titleRes) else item.title) },
        text = {
            val selected = entryValues.indexOf(value)
            RadioGroup(entries, selected) { newIndex ->
                value = entryValues[newIndex]
                item.value = value
                onValueChange(value)
            }
        },
        buttons = { },
        onDismissRequest = {
            onValueChange(value)
        }
    )
}


@Preview("InCarScreen Light")
@Composable
fun InCarScreenLight() {
    CarWidgetTheme(darkTheme = false) {
        BackgroundSurface {
            PreferencesScreen(listOf(
                PreferenceItem.Category(title ="Category"),
                PreferenceItem.Text(title ="Bluetooth device", summary = "Choose bluetooth device which enable InCar mode"),
                PreferenceItem.CheckBox(checked = true, title ="Keep screen On", summary = "When checked, prevents screen from automatically turning off"),
                PreferenceItem.Switch(checked = true, title ="Route to speaker", summary = "Route all incoming calls to phones speaker"),
            ), onClick = {})
        }
    }
}

@Preview("InCarScreen Dark")
@Composable
fun InCarScreenDark() {
    CarWidgetTheme(darkTheme = true) {
        BackgroundSurface {
            PreferencesScreen(listOf(
                PreferenceItem.Category(title ="Category"),
                PreferenceItem.Text(title ="Bluetooth device", summary = "Choose bluetooth device which enable InCar mode"),
                PreferenceItem.CheckBox(checked = true, title ="Keep screen On", summary = "When checked, prevents screen from automatically turning off"),
                PreferenceItem.Switch(checked = true, title ="Route to speaker", summary = "Route all incoming calls to phones speaker"),
            ), onClick = {})
        }
    }
}


package info.anodsplace.carwidget.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategory(@StringRes title: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = title),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.caption
    )
}

@Composable
fun Preference(@StringRes title: Int, @StringRes summary: Int, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = modifier
                .weight(8f)
                .clickable(onClick = onClick)
        ) {
            Text(text = stringResource(id = title), style = MaterialTheme.typography.subtitle1)
            if (summary != 0) {
                Text(text = stringResource(id = summary), style = MaterialTheme.typography.body2)
            }
        }
        Box(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PreferenceSwitch(checked: Boolean, @StringRes title: Int, @StringRes summary: Int, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
    Preference(title, summary, modifier, onClick = {
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
fun PreferenceCheckbox(checked: Boolean, @StringRes title: Int, @StringRes summary: Int, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
    Preference(title, summary, modifier, onClick = {
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
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(preferences.size) { index ->
            when (val item = preferences[index]) {
                is PreferenceItem.Category -> PreferenceCategory(title = item.title)
                is PreferenceItem.CheckBox -> {
                    val checked = remember { mutableStateOf(item.checked) }
                    PreferenceCheckbox(checked = checked.value, title = item.title, summary = item.summary, onCheckedChange = { newChecked ->
                        item.checked = newChecked
                        checked.value = newChecked
                        onClick(item)
                    })
                }
                is PreferenceItem.List -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
                is PreferenceItem.Switch -> {
                    val checked = remember { mutableStateOf(item.checked) }
                    PreferenceSwitch(checked = checked.value, title = item.title, summary = item.summary, onCheckedChange = { newChecked ->
                        item.checked = newChecked
                        checked.value = newChecked
                        onClick(item)
                    })
                }
                is PreferenceItem.Text -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
            }
        }
    }
}
package info.anodsplace.carwidget.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.compose.CarWidgetTheme
import info.anodsplace.carwidget.preferences.screen.createCarScreenItems
import java.util.*

@Composable
fun PreferenceCategory(@StringRes title: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = title),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
fun Preference(@StringRes title: Int, @StringRes summary: Int, modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Text(text = stringResource(id = title), style = MaterialTheme.typography.subtitle1)
        if (summary != 0) {
            Text(text = stringResource(id = summary), style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun PreferencesScreen(items: List<PreferenceItem>, onClick: (item: PreferenceItem) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        for (item in items) {
            when (item) {
                is PreferenceItem.Category -> PreferenceCategory(title = item.title)
                is PreferenceItem.CheckBox -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
                is PreferenceItem.List -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
                is PreferenceItem.Switch -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
                is PreferenceItem.Text -> Preference(title = item.title, summary = item.summary, onClick = { onClick(item) }) { }
            }
        }
    }
}

@Preview("PreviewPreferencesScreen")
@Composable
fun PreviewPreferencesScreen() {
    CarWidgetTheme(darkTheme = false) {
        Surface {
            PreferencesScreen(createCarScreenItems(), onClick = {})
        }
    }
}
package info.anodsplace.carwidget.compose

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.carwidget.prefs.IntentActions
import androidx.compose.material.Text

class SingleScreenState(
        val title: String,
        val items: Map<String, String>,
)

@Composable
fun SingleScreenList(items: Map<String, String>, modifier: Modifier = Modifier, onSelect: (key: String, value: String) -> Unit) {
    ScrollableColumn(modifier = modifier) {
        for (item in items) {
            Row(
                    modifier = Modifier
                            .clickable(onClick = { onSelect(item.key, item.value) })
            ) {
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
        Spacer(modifier = Modifier.preferredHeight(8.dp))
    }
}


@Composable
fun SingleListScreen(state: SingleScreenState, onSelect: (key: String, value: String) -> Unit) {
    Surface(
            modifier = Modifier.padding(16.dp),
            elevation = 2.dp,
            color = MaterialTheme.colors.surface) {
        Column {
            Box(modifier = Modifier.padding(16.dp)) {
                Providers(AmbientContentAlpha provides ContentAlpha.high) {
                    Text(text = state.title, style = MaterialTheme.typography.subtitle1)
                }
            }
            SingleScreenList(
                    items = state.items,
                    modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                    onSelect = onSelect
            )
        }
    }
}

@Preview
@Composable
fun SingleListScreenPreview() {
    val state = SingleScreenState("Action", IntentActions)
    CarWidgetTheme(darkTheme = false) {
        Box(contentAlignment = Alignment.Center) {
            SingleListScreen(state, onSelect = { _, _ ->  })
        }
    }
}
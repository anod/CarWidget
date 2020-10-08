package info.anodsplace.carwidget.compose

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import info.anodsplace.framework.livedata.SingleLiveEvent

@Composable
fun AppBar(action: SingleLiveEvent<UiAction>) {
    TopAppBar(
            title = {
                Text(text = "Edit Intent")
            },
            navigationIcon = {
                IconButton(onClick = { action.value = UiAction.OnBackNav }) {
                    Icon(Icons.Filled.ChevronLeft)
                }
            },
            // We need to balance the navigation icon, so we add a spacer.
            actions = {
                Spacer(modifier = Modifier.preferredWidth(68.dp))
            },
            backgroundColor = MaterialTheme.colors.primary,
            elevation = 0.dp
    )
}

@Preview("Dark AppBar")
@Composable
fun PreviewAppBarDark() {
    CarWidgetTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.primary) {
            Scaffold(
                    topBar = { AppBar(SingleLiveEvent()) },
                    bodyContent = { Text(text = "Content") }
            )
        }
    }
}

@Preview("Light AppBar")
@Composable
fun PreviewAppBarLight() {
    CarWidgetTheme(darkTheme = false) {
        Surface() {
            Scaffold(
                    topBar = { AppBar(SingleLiveEvent()) },
                    bodyContent = { Text(text = "Content") }
            )
        }
    }
}
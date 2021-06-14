package info.anodsplace.carwidget.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.CarWidgetTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun CarWidgetToolbar(action: MutableSharedFlow<UiAction>) {
    val scope = rememberCoroutineScope()

    TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.edit_intent))
            },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        action.emit(UiAction.OnBackNav)
                    }
                }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(id = R.string.back))
                }
            },
            // We need to balance the navigation icon, so we add a spacer.
            actions = {
                Spacer(modifier = Modifier.width(68.dp))
            },
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
    )
}

@Preview("Dark AppBar")
@Composable
fun PreviewAppBarDark() {
    CarWidgetTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.primary) {
            Scaffold(
                    topBar = { CarWidgetToolbar(MutableSharedFlow()) },
                    content = { Text(text = "Content") }
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
                    topBar = { CarWidgetToolbar(MutableSharedFlow()) },
                    content = { Text(text = "Content") }
            )
        }
    }
}
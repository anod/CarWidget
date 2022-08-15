package info.anodsplace.carwidget.screens

import android.app.UiModeManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarWidgetToolbar(action: MutableSharedFlow<UiAction>) {
    val scope = rememberCoroutineScope()

    SmallTopAppBar(
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
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Dark AppBar")
@Composable
fun PreviewAppBarDark() {
    CarWidgetTheme(uiMode = UiModeManager.MODE_NIGHT_YES) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Scaffold(
                    topBar = { CarWidgetToolbar(MutableSharedFlow()) },
                    content = { paddingValues -> Text(text = "Content", modifier = Modifier.padding(paddingValues)) }
            )
        }
    }
}
package info.anodsplace.carwidget.screens

import android.app.UiModeManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.carwidget.CarWidgetTheme
import info.anodsplace.carwidget.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarWidgetToolbar(onBackNav: () -> Unit) {
    SmallTopAppBar(
            title = {
                Text(text = stringResource(id = R.string.edit_intent))
            },
            navigationIcon = {
                IconButton(onClick = { onBackNav() }) {
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
                    topBar = { CarWidgetToolbar(onBackNav = { }) },
                    content = { paddingValues -> Text(text = "Content", modifier = Modifier.padding(paddingValues)) }
            )
        }
    }
}
package info.anodsplace.carwidget.permissions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import info.anodsplace.carwidget.content.R
import info.anodsplace.carwidget.main.MainViewEvent
import info.anodsplace.carwidget.main.MainViewState
import info.anodsplace.compose.RequestPermissionsScreen
import info.anodsplace.compose.findActivity

@Composable
fun PermissionsScreen(screenState: MainViewState, onEvent: (MainViewEvent) -> Unit) {
    val context = LocalContext.current
    val permissionsViewModel: PermissionsViewModel = viewModel(
        factory = PermissionsViewModel.Factory(
            requiredPermissions = screenState.requiredPermissions,
            activity = context.findActivity()
        )
    )
    val viewState by permissionsViewModel.viewStates.collectAsState(initial = permissionsViewModel.viewState)
    RequestPermissions(
        viewState = viewState,
        onEvent = {
            permissionsViewModel.handleEvent(it)
            if (it is PermissionsViewEvent.RequestPermissionResult) {
                onEvent(MainViewEvent.PermissionAcquired)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestPermissions(viewState: PermissionsViewState, onEvent: (PermissionsViewEvent) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = info.anodsplace.carwidget.R.drawable.ic_launcher_48),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            val context = LocalContext.current
            RequestPermissionsScreen(
                modifier = Modifier.padding(16.dp).align(Alignment.TopCenter),
                input = viewState.missingPermissions,
                screenDescription = viewState.screenDescription,
                onResult = { _, exception ->
                    onEvent(PermissionsViewEvent.RequestPermissionResult(context.findActivity(), exception))
                }
            )
        }
    }
}
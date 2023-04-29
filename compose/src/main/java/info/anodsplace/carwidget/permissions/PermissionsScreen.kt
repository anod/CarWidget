package info.anodsplace.carwidget.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
    RequestPermissionsScreen(
        input = viewState.missingPermissions,
        screenDescription = viewState.screenDescription
    ) {
        if (permissionsViewModel.updatePermissions(context.findActivity())) {
            onEvent(MainViewEvent.PermissionAcquired)
        }
    }
}
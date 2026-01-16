package info.anodsplace.carwidget.permissions

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import info.anodsplace.carwidget.content.R
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.compose.RequestPermissionsScreen
import info.anodsplace.compose.RequestPermissionsScreenDescription
import info.anodsplace.permissions.AppPermission


@Composable
fun RequestPermissionsDialog(
    missingPermissions: List<PermissionDescription>,
    onResult: (List<AppPermission>, exception: Exception?) -> Unit
) {
    Dialog(
        onDismissRequest = { onResult(emptyList(), null) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface {
            RequestPermissionsScreen(
                modifier = Modifier.padding(16.dp),
                input = missingPermissions.map { it },
                screenDescription = RequestPermissionsScreenDescription(
                    titleRes = R.string.missing_required_permissions,
                    allowAccessRes = R.string.allow_access,
                    cancelRes = android.R.string.cancel
                ),
                onResult = onResult
            )
        }
    }
}
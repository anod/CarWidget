package info.anodsplace.carwidget.permissions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.carwidget.content.R
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.compose.RequestPermissionsScreenDescription
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.viewmodel.BaseFlowViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

fun PermissionDescriptionItem.toPermissionDescription(): PermissionDescription = PermissionDescription(
    permission = AppPermissions.fromValue(this.permission),
    iconRes = this.iconsRes,
    titleRes = this.titleRes,
    descRes = this.descRes
)

@Immutable
data class PermissionsViewState(
    val screenDescription: RequestPermissionsScreenDescription,
    val missingPermissions: List<PermissionDescription>
)

sealed interface PermissionsViewEvent {
}

sealed interface PermissionsViewAction

class PermissionsViewModel(
    initialPermission: List<AppPermission>,
    private val permissionDescriptionsMap: Map<AppPermission, PermissionDescription>,
    private val permissionChecker: PermissionChecker,
) : BaseFlowViewModel<PermissionsViewState, PermissionsViewEvent, PermissionsViewAction>() {

    class Factory(
        private val requiredPermissions: List<AppPermission>,
        private val activity: ComponentActivity
    ) : ViewModelProvider.Factory, KoinComponent {

        private val permissionChecker: PermissionChecker by inject()
        private val permissionDescriptions: List<PermissionDescriptionItem> by inject(named("permissionDescriptions"))
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PermissionsViewModel(
                permissionChecker.check(requiredPermissions, activity),
                permissionDescriptions.associateBy({ AppPermissions.fromValue(it.permission)}, { it.toPermissionDescription() }),
                permissionChecker,
            ) as T
        }
    }

    init {
        viewState = PermissionsViewState(
            screenDescription = RequestPermissionsScreenDescription(
                titleRes = R.string.missing_required_permissions,
                allowAccessRes = R.string.allow_access
            ),
            missingPermissions = initialPermission
                .mapNotNull { permissionDescriptionsMap[it] }
        )
    }

    override fun handleEvent(event: PermissionsViewEvent) { }

    fun updatePermissions(activity: ComponentActivity): Boolean {
        val requiredPermissions = permissionChecker.check(activity)
        if (requiredPermissions.isNotEmpty()) {
            viewState = viewState.copy(
                missingPermissions = requiredPermissions.mapNotNull { permissionDescriptionsMap[it] }
            )
            return false
        }
        return true
    }
}
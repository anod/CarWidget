package info.anodsplace.carwidget.screens.main

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.carwidget.permissions.PermissionChecker
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.compose.RequestPermissionsScreenDescription
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

fun PermissionDescriptionItem.toPermissionDescription(): PermissionDescription = PermissionDescription(
    permission = AppPermissions.fromValue(this.permission),
    iconsRes = this.iconsRes,
    titleRes = this.titleRes,
    descRes = this.descRes
)

class PermissionsViewModel(
    initialPermission: List<AppPermission>,
    private val permissionDescriptionsMap: Map<AppPermission, PermissionDescription>,
    private val permissionChecker: PermissionChecker,
    application: Application
) : AndroidViewModel(application) {

    class Factory(
        private val requiredPermissions: List<AppPermission>,
        private val activity: ComponentActivity
    ) : ViewModelProvider.Factory, KoinComponent {

        private val permissionChecker: PermissionChecker by inject()
        private val permissionDescriptions: List<PermissionDescriptionItem> by inject(
            named("permissionDescriptions")
        )
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PermissionsViewModel(
                permissionChecker.check(requiredPermissions, activity),
                permissionDescriptions.associateBy({ AppPermissions.fromValue(it.permission)}, { it.toPermissionDescription() }),
                permissionChecker,
                activity.applicationContext as Application
            ) as T
        }
    }

    val screenDescription = RequestPermissionsScreenDescription(
        descRes = R.string.needs_permissions_to_work,
        titleRes = R.string.app_name,
        allowAccessRes = R.string.allow_access
    )

    val missingPermissions = initialPermission
        .mapNotNull { permissionDescriptionsMap[it] }
        .toMutableStateList()

    fun updatePermissions(activity: ComponentActivity): Boolean {
        val requiredPermissions = permissionChecker.check(activity)
        if (requiredPermissions.isNotEmpty()) {
            missingPermissions.clear()
            missingPermissions.addAll(requiredPermissions.mapNotNull { permissionDescriptionsMap[it] })
            return false
        }
        return true
    }

}
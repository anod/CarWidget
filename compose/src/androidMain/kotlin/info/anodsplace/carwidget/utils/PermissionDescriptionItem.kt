package info.anodsplace.carwidget.utils

import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.compose.PermissionDescription
import info.anodsplace.permissions.AppPermissions

fun PermissionDescriptionItem.toPermissionDescription(): PermissionDescription = PermissionDescription(
    permission = AppPermissions.fromValue(this.permission),
    iconRes = this.iconsRes,
    titleRes = this.titleRes,
    descRes = this.descRes
)

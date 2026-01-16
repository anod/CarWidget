package info.anodsplace.carwidget.content

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class PermissionDescriptionItem(
    val permission: String,
    @get:DrawableRes
    val iconsRes: Int,
    @get:StringRes
    val titleRes: Int,
    @get:StringRes
    val descRes: Int
)
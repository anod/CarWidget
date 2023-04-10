package info.anodsplace.carwidget.screens

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf

sealed class NavItem(val route: String, val parent: NavItem? = null) {
    override fun hashCode(): Int = hashCodeOf(route, parent)
    override fun equals(other: Any?): Boolean = equalsHash(this, other)

    object Wizard : NavItem(route = "wizard", parent = null)

    object PermissionsRequest : NavItem(route = "permissions/request", parent = null)

    abstract class Tab(route: String, @StringRes val resourceId: Int, val icon: ImageVector) : NavItem(route) {

        object Widgets : Tab(route = "widgets/list", resourceId = info.anodsplace.carwidget.content.R.string.widgets, icon = Icons.Filled.Widgets)

        object CurrentWidget : Tab(route = "widgets/current", resourceId = info.anodsplace.carwidget.content.R.string.shortcuts, icon = Icons.Filled.Widgets) {
            object Skin : NavItem(route = "widgets/current/skin", parent = CurrentWidget)

            object EditShortcut : NavItem(route = "widgets/current/edit/{shortcut_id}/{pos_id}", parent = CurrentWidget) {
                val arguments: List<NamedNavArgument> = listOf(
                    navArgument("shortcut_id") { type = NavType.LongType },
                    navArgument("pos_id") { type = NavType.IntType },
                )
                val deepLinks: List<NavDeepLink> = listOf(
                    navDeepLink { uriPattern = Deeplink.EditShortcut.uriPattern }
                )
                class Args(val shortcutId: Long, val position: Int) {
                    constructor(bundle: Bundle?) : this(
                        shortcutId = bundle?.getLong("shortcut_id") ?: 0,
                        position = bundle?.getInt("pos_id") ?: 0
                    )
                }
            }
            object EditWidgetButton : NavItem(route = "widgets/current/edit-button/{btn_id}", parent = CurrentWidget) {
                val arguments: List<NamedNavArgument> = listOf(
                    navArgument("btn_id") { type = NavType.IntType },
                )
                val deepLinks: List<NavDeepLink> = listOf(
                    navDeepLink { uriPattern = Deeplink.EditWidgetButton.uriPattern }
                )
                class Args(val buttonId: Int) {
                    constructor(bundle: Bundle?) : this(
                        buttonId = bundle?.getInt("btn_id") ?: 0
                    )
                }
            }
        }

        object WidgetCustomize: Tab(route = "customize/widgets/current", resourceId = info.anodsplace.carwidget.content.R.string.customize, icon = Icons.Filled.Palette )

        object InCar : Tab(route = "incar", resourceId = info.anodsplace.carwidget.content.R.string.pref_incar_mode_title, Icons.Filled.DirectionsCar) {
            object Main : NavItem(route = "incar/main", parent = InCar)
            object Bluetooth : NavItem(route = "incar/bluetooh", parent = InCar)
        }

        object About : Tab(route = "about", resourceId = info.anodsplace.carwidget.content.R.string.info, Icons.Outlined.Info)
    }
}
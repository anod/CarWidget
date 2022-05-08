package info.anodsplace.carwidget.screens

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.Deeplink
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.di.isValid

sealed class NavItem(val route: String, val parent: NavItem? = null) {
    abstract class TabItem(route: String, @StringRes val resourceId: Int, val icon: ImageVector) : NavItem(route)

    object Widgets : TabItem("widgets/list", R.string.widgets, Icons.Filled.Widgets)

    object CurrentWidget : TabItem("widgets/current", R.string.current_widget, Icons.Filled.Widgets) {
        object Skin : NavItem("widgets/current/skin", parent = CurrentWidget)
        object MoreSettings: NavItem("widgets/current/more", parent = CurrentWidget)
        object EditShortcut : NavItem("widgets/current/edit/{shortcut_id}/{pos_id}", parent = CurrentWidget) {
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
        object EditWidgetButton : NavItem("widgets/current/edit-button/{btn_id}", parent = CurrentWidget) {
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

    object InCar : TabItem("incar", R.string.pref_incar_mode_title, Icons.Filled.DirectionsCar) {
        object Main : NavItem("incar/main", parent = InCar)
        object Bluetooth : NavItem("incar/bluetooh", parent = InCar)
        object Media : NavItem("incar/media", parent = InCar)
        object More : NavItem("incar/more", parent = InCar)
    }

    object About : TabItem("about", R.string.info, Icons.Outlined.Info)

    companion object {
        fun startDestination(appWidgetIdScope: AppWidgetIdScope?) = if (appWidgetIdScope.isValid)
            CurrentWidget.route else Widgets.route

        fun startRoute(appWidgetIdScope: AppWidgetIdScope?): String? = if (appWidgetIdScope.isValid)
            CurrentWidget.Skin.route else null
    }
}
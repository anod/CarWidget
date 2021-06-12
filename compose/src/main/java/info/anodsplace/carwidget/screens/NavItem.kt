package info.anodsplace.carwidget.screens

import android.appwidget.AppWidgetManager
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import info.anodsplace.carwidget.R

sealed class NavItem(val route: String, val parent: NavItem? = null) {
    abstract class TabItem(route: String, @StringRes val resourceId: Int, val icon: ImageVector) : NavItem(route)

    object Widgets : TabItem("widgets", R.string.widgets, Icons.Filled.Widgets)
    object InCar : TabItem("incar", R.string.pref_incar_mode_title, Icons.Filled.DirectionsCar) {
        object Main : NavItem("incar/main", parent = InCar)
        object Bluetooth : NavItem("incar/bluetooh", parent = InCar)
        object Media : NavItem("incar/media", parent = InCar)
        object More : NavItem("incar/more", parent = InCar)
    }
    object CurrentWidget : TabItem("widgets/{appWidgetId}", R.string.current_widget, Icons.Filled.Widgets) {
        fun forAppWidgetId(appWidgetId: Int) = "widgets/$appWidgetId"
    }
    object Info : TabItem("info", R.string.info, Icons.Outlined.Info)

    companion object {
        fun startDestination(appWidgetId: Int) = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            Widgets.route else CurrentWidget.route

        fun startRoute(appWidgetId: Int): String? = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            null else "widgets/$appWidgetId"
    }
}

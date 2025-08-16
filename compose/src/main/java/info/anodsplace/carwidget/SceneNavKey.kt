package info.anodsplace.carwidget

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import info.anodsplace.carwidget.content.Deeplink
import kotlinx.serialization.Serializable

interface TabNavKey : SceneNavKey {
    val routeId: String
    @get:StringRes
    val title: Int
    val icon: ImageVector
}

interface InnerSceneNavKey : SceneNavKey {
    val parent: SceneNavKey?
}

sealed interface RouteNameSpace {
    @Serializable
    data object Default: RouteNameSpace
    @Serializable
    data object AppWidget: RouteNameSpace
}

sealed interface SceneNavKey {

    @Serializable
    data object Wizard : SceneNavKey

    @Serializable
    data object PermissionsRequest : SceneNavKey

    @Serializable
    data object Widgets : TabNavKey {
        override val routeId: String = "Widgets"
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.widgets
        override val icon: ImageVector
            get() = Icons.Filled.Widgets
    }

    @Serializable
    data object CurrentWidget : TabNavKey {
        override val routeId: String = "CurrentWidget"
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.widgets
        override val icon: ImageVector
            get() = Icons.Filled.Widgets
    }

    @Serializable
    data object WidgetCustomize: TabNavKey {
        override val routeId: String = "WidgetCustomize"
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.customize
        override val icon: ImageVector
            get() = Icons.Filled.Palette
    }

    @Serializable
    data object InCar : TabNavKey {
        override val routeId: String = "InCar"
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.pref_incar_mode_title
        override val icon: ImageVector
            get() = Icons.Filled.DirectionsCar
    }

    @Serializable
    data object About : TabNavKey {
        override val routeId: String = "About"
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.info
        override val icon: ImageVector
            get() = Icons.Outlined.Info
    }

    @Serializable
    data object Skin : InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidget
    }

    @Serializable
    data class EditShortcut(
        val shortcutId: Long,
        val position: Int
    ) : InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidget
        companion object {
            val deepLinks: List<NavDeepLink> = listOf(
                navDeepLink { uriPattern = Deeplink.EditShortcut.uriPattern }
            )
        }
    }

    @Serializable
    data class EditWidgetButton(
        val buttonId: Int
    ) : InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidget
    }

    @Serializable
    data object Main : InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCar
    }
    @Serializable
    data object Bluetooth : InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCar
    }
}
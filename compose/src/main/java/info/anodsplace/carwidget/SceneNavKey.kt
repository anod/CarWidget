package info.anodsplace.carwidget

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface TabNavKey {
    @get:StringRes
    val title: Int
    val icon: ImageVector
}

interface InnerSceneNavKey {
    val parent: SceneNavKey?
    val showBackNavigation: Boolean
}

@Serializable
sealed interface RouteNameSpace {
    @Serializable
    data object Default: RouteNameSpace
    @Serializable
    data object AppWidget: RouteNameSpace
    @Serializable
    data object Overlay: RouteNameSpace
}

@Serializable
sealed interface SceneNavKey : NavKey {

    @Serializable
    data object Wizard : SceneNavKey

    @Serializable
    data object PermissionsRequest : SceneNavKey

    @Serializable
    data object WidgetsTab : SceneNavKey, TabNavKey {
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.widgets
        override val icon: ImageVector
            get() = Icons.Filled.Widgets
    }

    @Serializable
    data object CurrentWidgetTab : SceneNavKey, TabNavKey {
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.shortcuts
        override val icon: ImageVector
            get() = Icons.Filled.Widgets
    }

    @Serializable
    data object WidgetCustomize: SceneNavKey, TabNavKey {
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.customize
        override val icon: ImageVector
            get() = Icons.Filled.Palette
    }

    @Serializable
    data object InCarTab : SceneNavKey, TabNavKey {
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.pref_incar_mode_title
        override val icon: ImageVector
            get() = Icons.Filled.DirectionsCar
    }

    @Serializable
    data object AboutTab : SceneNavKey, TabNavKey {
        override val title: Int
            get() = info.anodsplace.carwidget.content.R.string.info
        override val icon: ImageVector
            get() = Icons.Outlined.Info
    }

    @Serializable
    data object Shortcuts : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
        override val showBackNavigation: Boolean = false
    }

    @Serializable
    data class EditShortcut(
        val shortcutId: Long,
        val position: Int
    ) : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
        override val showBackNavigation: Boolean = true
    }

    @Serializable
    data class EditWidgetButton(
        val buttonId: Int
    ) : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
        override val showBackNavigation: Boolean = true
    }

    @Serializable
    data object PlayMediaButton : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
        override val showBackNavigation: Boolean = true
    }

    @Serializable
    data object InCarMain : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCarTab
        override val showBackNavigation: Boolean = false
    }

    @Serializable
    data object InCarBluetooth : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCarTab
        override val showBackNavigation: Boolean = true
    }

}
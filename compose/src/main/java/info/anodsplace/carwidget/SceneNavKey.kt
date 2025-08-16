package info.anodsplace.carwidget

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import info.anodsplace.carwidget.content.Deeplink
import kotlinx.serialization.Serializable

interface TabNavKey {
    @get:StringRes
    val title: Int
    val icon: ImageVector
}

interface InnerSceneNavKey {
    val parent: SceneNavKey?
}

@Serializable
sealed interface RouteNameSpace {
    @Serializable
    data object Default: RouteNameSpace
    @Serializable
    data object AppWidget: RouteNameSpace
}

private val pathToNavKeyMap = listOf(
    SceneNavKey.WidgetsTab.serializer().descriptor.serialName to SceneNavKey.WidgetsTab::class,
    SceneNavKey.InCarTab.serializer().descriptor.serialName to SceneNavKey.InCarTab::class,
    SceneNavKey.InCarMain.serializer().descriptor.serialName to SceneNavKey.InCarMain::class,
    SceneNavKey.InCarBluetooth.serializer().descriptor.serialName to SceneNavKey.InCarBluetooth::class,
    SceneNavKey.AboutTab.serializer().descriptor.serialName to SceneNavKey.AboutTab::class,

    SceneNavKey.CurrentWidgetTab.serializer().descriptor.serialName to SceneNavKey.CurrentWidgetTab::class,
    SceneNavKey.WidgetCustomize.serializer().descriptor.serialName to SceneNavKey.WidgetCustomize::class,
    SceneNavKey.Shortcuts.serializer().descriptor.serialName to SceneNavKey.Shortcuts::class,

    SceneNavKey.EditShortcut.serializer().descriptor.serialName to SceneNavKey.EditShortcut::class,
    SceneNavKey.EditWidgetButton.serializer().descriptor.serialName to SceneNavKey.EditWidgetButton::class,
    SceneNavKey.Wizard.serializer().descriptor.serialName to SceneNavKey.Wizard::class,
    SceneNavKey.PermissionsRequest.serializer().descriptor.serialName to SceneNavKey.PermissionsRequest::class,
)

@Suppress("UNCHECKED_CAST")
fun NavBackStackEntry.toSceneNavKey(): SceneNavKey? {
    val route = this.destination.route ?: return null
    val kclass = pathToNavKeyMap.firstOrNull { route.startsWith(it.first, ignoreCase = true) }?.second ?: return null
    return this.toRoute(kclass)
}

@Serializable
sealed interface SceneNavKey {

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
    }

    @Serializable
    data class EditShortcut(
        val shortcutId: Long,
        val position: Int
    ) : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
        companion object {
            val deepLinks: List<NavDeepLink> = listOf(
                navDeepLink { uriPattern = Deeplink.EditShortcut.uriPattern }
            )
        }
    }

    @Serializable
    data class EditWidgetButton(
        val buttonId: Int
    ) : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = CurrentWidgetTab
    }

    @Serializable
    data object InCarMain : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCarTab
    }

    @Serializable
    data object InCarBluetooth : SceneNavKey, InnerSceneNavKey {
        override val parent: SceneNavKey? get() = InCarTab
    }
}
package info.anodsplace.carwidget.content.db

import android.net.Uri
import android.provider.BaseColumns
import info.anodsplace.carwidget.content.BuildConfig


/**
 * Settings related utilities.
 */
class LauncherSettings {

    class Favorites : BaseColumns {
        companion object {
            const val TITLE = "title"
            const val INTENT = "intent"
            const val ITEM_TYPE = "itemType"
            const val ICON_TYPE = "iconType"
            const val ICON_PACKAGE = "iconPackage"
            const val ICON_RESOURCE = "iconResource"
            const val ICON = "icon"
            const val IS_CUSTOM_ICON = "isCustomIcon"

            const val ITEM_TYPE_APPLICATION = 0
            const val ITEM_TYPE_SHORTCUT = 1

            const val ICON_TYPE_RESOURCE = 0
            const val ICON_TYPE_BITMAP = 1

            val AUTHORITY = if (BuildConfig.DEBUG)
                "com.anod.car.home.free.debug.shortcutsprovider"
            else
                "com.anod.car.home.free.shortcutsprovider"

            fun getContentUri(packageName: String, id: Long): Uri {
                return Uri.parse("$CONTENT_PREFIX$AUTHORITY/$TABLE_FAVORITES/$id")
            }

            private const val CONTENT_PREFIX = "content://"
            private const val TABLE_FAVORITES = "favorites"
        }
    }
}

package info.anodsplace.carwidget.content.db

import android.net.Uri
import android.provider.BaseColumns
import info.anodsplace.carwidget.content.BuildConfig

import info.anodsplace.carwidget.content.Version

/**
 * Settings related utilities.
 */
class LauncherSettings {

    class Favorites : BaseColumns {
        companion object {

            private const val CONTENT_PREFIX = "content://"
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

            /**
             * The content:// style URL for this table
             */
            fun getContentUri(packageName: String): Uri {
                return if (Version.isFree(packageName)) {
                    Uri.parse(
                        CONTENT_PREFIX + AUTHORITY_FREE + "/"
                            + TABLE_FAVORITES)
                } else Uri.parse(
                    CONTENT_PREFIX + AUTHORITY_PRO + "/"
                        + TABLE_FAVORITES)
            }

            /**
             * The content:// style URL for a given row, identified by its id.
             *
             * @param id The row id.
             * @return The unique content URL for the specified row.
             */
            fun getContentUri(packageName: String, id: Long): Uri {
                return if (Version.isFree(packageName)) {
                    Uri.parse(
                        CONTENT_PREFIX + AUTHORITY_FREE + "/"
                            + TABLE_FAVORITES + "/" + id)
                } else Uri.parse(
                    CONTENT_PREFIX + AUTHORITY_PRO + "/"
                        + TABLE_FAVORITES + "/" + id)
            }


            private val AUTHORITY_FREE = if (BuildConfig.DEBUG)
                "com.anod.car.home.free.debug.shortcutsprovider"
            else
                "com.anod.car.home.free.shortcutsprovider"

            private val AUTHORITY_PRO = if (BuildConfig.DEBUG)
                "com.anod.car.home.pro.debug.shortcutsprovider"
            else
                "com.anod.car.home.pro.shortcutsprovider"

            private const val TABLE_FAVORITES = "favorites"
        }

    }
}

package info.anodsplace.carwidget.db

import android.net.Uri
import android.provider.BaseColumns

import info.anodsplace.carwidget.content.LauncherContentProvider
import info.anodsplace.carwidget.utils.Version

/**
 * Settings related utilities.
 */
class LauncherSettings {

    class Favorites : BaseColumns {
        companion object {

            private const val CONTENT_PREFIX = "content://"

            /**
             * Descriptive name of the gesture that can be displayed to the user.
             * <P>
             * Type: TEXT
            </P> *
             */
            const val TITLE = "title"

            /**
             * The Intent URL of the gesture, describing what it points to. This
             * value is given to
             * [android.content.Intent.parseUri] to create an
             * Intent that can be launched.
             * <P>
             * Type: TEXT
            </P> *
             */
            const val INTENT = "intent"

            /**
             * The type of the gesture
             *
             * <P>
             * Type: INTEGER
            </P> *
             */
            const val ITEM_TYPE = "itemType"

            /**
             * The gesture is an application
             */
            const val ITEM_TYPE_APPLICATION = 0

            /**
             * The gesture is an application created shortcut
             */
            const val ITEM_TYPE_SHORTCUT = 1

            /**
             * The icon type.
             * <P>
             * Type: INTEGER
            </P> *
             */
            const val ICON_TYPE = "iconType"

            /**
             * The icon is a resource identified by a package name and an integer
             * id.
             */
            const val ICON_TYPE_RESOURCE = 0

            /**
             * The icon is a bitmap.
             */
            const val ICON_TYPE_BITMAP = 1

            /**
             * The icon package name, if icon type is ICON_TYPE_RESOURCE.
             * <P>
             * Type: TEXT
            </P> *
             */
            const val ICON_PACKAGE = "iconPackage"

            /**
             * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
             * <P>
             * Type: TEXT
            </P> *
             */
            const val ICON_RESOURCE = "iconResource"

            /**
             * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
             * <P>
             * Type: BLOB
            </P> *
             */
            const val ICON = "icon"

            /**
             * Type: BOOLEAN
             */
            const val IS_CUSTOM_ICON = "isCustomIcon"

            /**
             * The content:// style URL for this table
             */
            fun getContentUri(packageName: String): Uri {
                return if (Version.isFree(packageName)) {
                    Uri.parse(
                        CONTENT_PREFIX + LauncherContentProvider.AUTHORITY_FREE + "/"
                            + LauncherContentProvider.TABLE_FAVORITES)
                } else Uri.parse(
                    CONTENT_PREFIX + LauncherContentProvider.AUTHORITY_PRO + "/"
                        + LauncherContentProvider.TABLE_FAVORITES)
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
                        CONTENT_PREFIX + LauncherContentProvider.AUTHORITY_FREE + "/"
                            + LauncherContentProvider.TABLE_FAVORITES + "/" + id)
                } else Uri.parse(
                    CONTENT_PREFIX + LauncherContentProvider.AUTHORITY_PRO + "/"
                        + LauncherContentProvider.TABLE_FAVORITES + "/" + id)
            }
        }

    }
}

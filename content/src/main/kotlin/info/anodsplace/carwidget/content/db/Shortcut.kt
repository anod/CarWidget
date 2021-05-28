package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import info.anodsplace.carwidget.content.preferences.WidgetInterface

/**
 * @author algavris
 * @date 22/08/2016.
 */

class Shortcut(
        /**
         * The id in the settings database for this item
         */
        val id: Long,
        /**
         * One of [LauncherSettings.Favorites.ITEM_TYPE_APPLICATION],
         * [LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT],
         */
        val itemType: Int,
        /**
         * The application name.
         */
        val title: CharSequence,
        val isCustomIcon: Boolean,
        /**
         * The intent used to start the application.
         */
        val intent: Intent) {

    val isApp: Boolean
        get() = itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION

    constructor(id: Long, item: Shortcut) : this(id, item.itemType, item.title, item.isCustomIcon, item.intent)

    companion object {
        const val idUnknown: Long = WidgetInterface.idUnknown

        /**
         * Creates the application intent based on a component name and various launch flags.
         * Sets [.itemType] to [LauncherSettings.Favorites.ITEM_TYPE_APPLICATION].
         *
         * @param title
         * @param className   the class name of the component representing the intent
         * @param launchFlags the launch flags
         */
        fun forActivity(id: Long, title: CharSequence, isCustomIcon: Boolean, className: ComponentName, launchFlags: Int): Shortcut {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.component = className
            intent.flags = launchFlags
            return Shortcut(id, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, title, isCustomIcon, intent)
        }
    }
}

fun Shortcut.iconUri(context: Context, adaptiveIconStyle: String): Uri {
    return LauncherSettings.Favorites.getContentUri(context.packageName, id).buildUpon()
    .appendQueryParameter("adaptiveIconStyle", adaptiveIconStyle)
    .build()
}
package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import coil.request.ImageRequest
import coil.request.Parameters
import info.anodsplace.carwidget.content.extentions.isDebugBuild
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.ktx.equalsHash
import info.anodsplace.ktx.hashCodeOf

/**
 * @author algavris
 * @date 22/08/2016.
 */
data class Shortcut(
        val id: Long,
        val position: Int,
        val itemType: Int,
        val title: CharSequence,
        val isCustomIcon: Boolean,
        val intent: Intent
) {

    val isApp: Boolean
        get() = itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION

    val isValid: Boolean
        get() = id != idUnknown

    constructor(id: Long, item: Shortcut) : this(id, item.position, item.itemType, item.title, item.isCustomIcon, item.intent)

    companion object {
        const val idUnknown: Long = WidgetInterface.idUnknown

        val unknown: Shortcut = Shortcut(idUnknown, 0, 0, "", false, Intent())
        /**
         * Creates the application intent based on a component name and various launch flags.
         * Sets [.itemType] to [LauncherSettings.Favorites.ITEM_TYPE_APPLICATION].
         *
         * @param title
         * @param className   the class name of the component representing the intent
         * @param launchFlags the launch flags
         */
        fun forActivity(id: Long, position: Int, title: CharSequence, isCustomIcon: Boolean, className: ComponentName, launchFlags: Int): Shortcut {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.component = className
            intent.flags = launchFlags
            return Shortcut(id, position, LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, title, isCustomIcon, intent)
        }
    }

    override fun equals(other: Any?): Boolean = equalsHash(this, other)

    override fun hashCode(): Int = hashCodeOf(
        id, position, itemType, title, isCustomIcon, intent.toUri(0)
    )
}

fun Shortcut.iconUri(isDebug: Boolean, adaptiveIconStyle: String, skinName: String = ""): Uri {
    return LauncherSettings.Favorites.getContentUri(isDebug, id).buildUpon()
        .appendQueryParameter("skin", skinName)
        .appendQueryParameter("adaptiveIconStyle", adaptiveIconStyle)
        .build()
}

fun Shortcut.toImageRequest(context: Context, adaptiveIconStyle: String, skinName: String = "", iconVersion: Int = -1): ImageRequest = ImageRequest.Builder(context)
    .data(iconUri(context.isDebugBuild, adaptiveIconStyle, skinName)).apply {
       if (iconVersion > 0) {
           parameters(Parameters.Builder().set("version", iconVersion).build())
       }
    }
    .build()
package info.anodsplace.carwidget.content.db

import android.content.Intent
import android.graphics.Bitmap

/**
 * @author algavris
 * @date 22/08/2016.
 */
class ShortcutIcon(
        val id: Long,
       /**
        * Indicates whether the icon comes from an application's resource (if false)
        * or from a custom Bitmap (if true.)
        */
       val isCustom: Boolean,
       /**
        * Indicates whether we're using the default fallback icon instead of something from the
        * app.
        */
       val isFallback: Boolean,
       /**
        * If isShortcut=true and customIcon=false, this contains a reference to the
        * shortcut icon as an application's resource.
        */
       val resource: Intent.ShortcutIconResource?,
       /**
        * The application icon.
        */
       val bitmap: Bitmap
) {
    companion object {
        fun forActivity(id: Long, icon: Bitmap): ShortcutIcon {
            return ShortcutIcon(id, isCustom = false, isFallback = false, resource = null, bitmap = icon)
        }

        fun forFallbackIcon(id: Long, icon: Bitmap): ShortcutIcon {
            return ShortcutIcon(id, isCustom = false, isFallback = true, resource = null, bitmap = icon)
        }

        fun forCustomIcon(id: Long, icon: Bitmap): ShortcutIcon {
            return ShortcutIcon(id, isCustom = true, isFallback = false, resource = null, bitmap = icon)
        }

        fun forIconResource(id: Long, icon: Bitmap, res: Intent.ShortcutIconResource): ShortcutIcon {
            return ShortcutIcon(id, isCustom = false, isFallback = false, resource = res, bitmap = icon)
        }
    }
}

val SelectShortcutIcon.shortcutIconResource: Intent.ShortcutIconResource
    get() {
        return Intent.ShortcutIconResource().apply {
            packageName = iconPackage
            resourceName = iconResource
        }
    }
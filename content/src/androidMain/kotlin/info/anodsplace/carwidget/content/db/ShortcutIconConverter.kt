package info.anodsplace.carwidget.content.db

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.extentions.isLowMemoryDevice
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.graphics.BitmapCachedDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ShortcutIconConverter {
    suspend fun defaultFallbackIcon(): ShortcutIcon
    suspend fun convert(shortcutId: Long, itemType: Int, icon: ByteArray?, isCustomIcon: Boolean, iconType: Int, iconResource: Intent.ShortcutIconResource?) : ShortcutIcon

    class Default(private val context: Context) : ShortcutIconConverter {
        private val packageManager: PackageManager = context.packageManager
        private val decoder = BitmapCachedDecoder(
            iconBitmapSize = UtilitiesBitmap.getIconMaxSize(context),
            isLowMemoryDevice = context.isLowMemoryDevice
        )

        override suspend fun defaultFallbackIcon(): ShortcutIcon {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return ShortcutIcon.forFallbackIcon(0, icon)
        }

        override suspend fun convert(shortcutId: Long, itemType: Int, icon: ByteArray?, isCustomIcon: Boolean, iconType: Int, iconResource: Intent.ShortcutIconResource?): ShortcutIcon = withContext(Dispatchers.Default) {
            var shortcutIcon: ShortcutIcon? = null
            try {
                shortcutIcon = when {
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION -> {
                        decoder.toBitmap(icon)
                            ?.let { bitmap ->
                                if (isCustomIcon) {
                                    ShortcutIcon.forCustomIcon(shortcutId, bitmap)
                                } else {
                                    ShortcutIcon.forActivity(shortcutId, bitmap)
                                }
                            }
                    }
                    iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE && iconResource != null -> {
                        var bitmap: Bitmap? = null
                        // the resource
                        try {
                            val resources = packageManager.getResourcesForApplication(iconResource.packageName)
                            val resId = resources.getIdentifier(iconResource.resourceName, null, null)
                            if (resId != 0) {
                                val iconDrawable = ResourcesCompat.getDrawable(resources, resId, null)!!
                                bitmap = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            // drop this. we have other places to look for icons
                            AppLog.w("loadShortcutIcon: NameNotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                        } catch (e: Resources.NotFoundException) {
                            AppLog.w("loadShortcutIcon: NotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                        }

                        // the db
                        if (bitmap == null) {
                            bitmap = decoder.toBitmap(icon)
                        }
                        bitmap?.let { ShortcutIcon.forIconResource(shortcutId, it, isCustom = false, iconResource) }
                    }
                    iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP -> {
                        decoder.toBitmap(icon)
                            ?.let { bitmap -> ShortcutIcon.forCustomIcon(shortcutId, bitmap) }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                AppLog.e(e)
            }

            if (shortcutIcon == null) {
                val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
                return@withContext ShortcutIcon.forFallbackIcon(shortcutId, icon)
            }

            return@withContext shortcutIcon
        }
    }
}

suspend fun ShortcutIconConverter.toShortcutIcon(shortcutId: Long, row: SelectShortcutIcon?): ShortcutIcon {
    if (row == null) {
        return defaultFallbackIcon()
    }
    return convert(
        shortcutId = shortcutId,
        itemType = row.itemType,
        icon = row.icon,
        isCustomIcon = row.isCustomIcon,
        iconType = row.iconType,
        iconResource = row.shortcutIconResource
    )
}


suspend fun ShortcutIconConverter.toShortcutIcon(
    shortcutId: Long,
    row: SelectFolderShortcutIcon?
): ShortcutIcon {
    if (row == null) {
        return defaultFallbackIcon()
    }
    return convert(
        shortcutId = shortcutId,
        itemType = row.itemType,
        icon = row.icon,
        isCustomIcon = row.isCustomIcon,
        iconType = row.iconType,
        iconResource = row.shortcutIconResource
    )
}

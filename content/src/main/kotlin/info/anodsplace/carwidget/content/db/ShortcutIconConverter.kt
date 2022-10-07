package info.anodsplace.carwidget.content.db

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.extentions.isLowMemoryDevice
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.graphics.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ShortcutIconConverter {
    suspend fun convert(shortcutId: Long, row: SelectShortcutIcon?) : ShortcutIcon

    class Default(
            private val context: Context
    ) : ShortcutIconConverter {
        private val packageManager: PackageManager = context.packageManager
        private val bitmapOptions: BitmapFactory.Options

        init {
            val iconMaxSize = UtilitiesBitmap.getIconMaxSize(context)

            bitmapOptions = BitmapFactory.Options()
            bitmapOptions.outWidth = iconMaxSize
            bitmapOptions.outHeight = iconMaxSize
            bitmapOptions.inSampleSize = 1
            if (context.isLowMemoryDevice) {
                // Always prefer RGB_565 config for low res. If the bitmap has transparency, it will
                // automatically be loaded as ALPHA_8888.
                bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
            } else {
                bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        }

        override suspend fun convert(shortcutId: Long, row: SelectShortcutIcon?): ShortcutIcon = withContext(Dispatchers.IO) {

            if (row == null) {
                val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
                return@withContext ShortcutIcon.forFallbackIcon(shortcutId, icon)
            }

            var shortcutIcon: ShortcutIcon? = null
            try {
                val itemType = row.itemType

                var icon: Bitmap? = null
                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                    icon = try {
                        row.icon?.toBitmap(bitmapOptions)
                    } catch (e: Exception) {
                        AppLog.e(e)
                        null
                    }

                    if (icon != null) {
                        shortcutIcon = if (row.isCustomIcon) {
                            ShortcutIcon.forCustomIcon(shortcutId, icon)
                        } else {
                            ShortcutIcon.forActivity(shortcutId, icon)
                        }
                    }
                } else {
                    val iconType = row.iconType
                    if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                        val iconResource = Intent.ShortcutIconResource().apply {
                            packageName = row.iconPackage
                            resourceName = row.iconResource
                        }
                        // the resource
                        try {
                            val resources = packageManager.getResourcesForApplication(iconResource.packageName)
                            val resId = resources.getIdentifier(iconResource.resourceName, null, null)
                            if (resId > 0) {
                                val iconDrawable = ResourcesCompat.getDrawable(resources, resId, null)!!
                                icon = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            // drop this. we have other places to look for icons
                            AppLog.w("loadShortcutIcon: NameNotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                        } catch (e: Resources.NotFoundException) {
                            AppLog.w("loadShortcutIcon: NotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                        }

                        // the db
                        if (icon == null) {
                            icon = try {
                                row.icon?.toBitmap(bitmapOptions)
                            } catch (e: Exception) {
                                AppLog.e(e)
                                null
                            }
                        }
                        shortcutIcon = ShortcutIcon.forIconResource(shortcutId, icon!!, iconResource)
                    } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                        icon = try {
                            row.icon?.toBitmap(bitmapOptions)
                        } catch (e: Exception) {
                            AppLog.e(e)
                            null
                        }
                        if (icon != null) {
                            shortcutIcon = ShortcutIcon.forCustomIcon(shortcutId, icon)
                        }
                    }
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
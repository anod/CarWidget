package info.anodsplace.carwidget.content.db

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Database
import info.anodsplace.carwidget.content.extentions.isLowMemoryDevice
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import java.net.URISyntaxException

class ShortcutsDatabase(private val context: Context, private val db: Database) {

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

    fun loadShortcutIcon(shortcutUri: Uri): ShortcutIcon {
        val id = shortcutUri.lastPathSegment!!.toLong()
        val c = db.shortcutsQueries.selectIcon(id).executeAsOneOrNull()

        if (c == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        var shortcutIcon: ShortcutIcon? = null
        try {
            val itemType = c.itemType.toInt()

            var icon: Bitmap? = null
            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                icon = getIconFromCursor(c.icon ?: ByteArray(0), bitmapOptions)
                if (icon != null) {
                    shortcutIcon = if (c.isCustomIcon == 1L) {
                        ShortcutIcon.forCustomIcon(id, icon)
                    } else {
                        ShortcutIcon.forActivity(id, icon)
                    }
                }
            } else {
                val iconType = c.iconType.toInt()
                if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                    val iconResource = Intent.ShortcutIconResource().apply {
                        packageName = c.iconPackage
                        resourceName = c.iconResource
                    }
                    // the resource
                    try {
                        val resources = packageManager.getResourcesForApplication(iconResource.packageName)
                        val resId = resources.getIdentifier(iconResource.resourceName, null, null)
                        if (resId > 0) {
                            val iconDrawable = ResourcesCompat.getDrawable(resources, resId, null)!!
                            icon = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
                        }
                    } catch (e: NameNotFoundException) {
                        // drop this. we have other places to look for icons
                        AppLog.w("loadShortcutIcon: NameNotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                    } catch (e: NotFoundException) {
                        AppLog.w("loadShortcutIcon: NotFoundException ${iconResource.packageName}/${iconResource.resourceName}")
                    }

                    // the db
                    if (icon == null) {
                        icon = getIconFromCursor(c.icon ?: ByteArray(0), bitmapOptions)
                    }
                    shortcutIcon = ShortcutIcon.forIconResource(id, icon!!, iconResource)
                } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                    icon = getIconFromCursor(c.icon ?: ByteArray(0), bitmapOptions)
                    if (icon != null) {
                        shortcutIcon = ShortcutIcon.forCustomIcon(id, icon)
                    }
                }
            }

        } catch (e: Exception) {
            AppLog.e(e)
        }

        if (shortcutIcon == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        return shortcutIcon
    }

    fun loadShortcut(shortcutId: Long): Shortcut? {
        val c = db.shortcutsQueries.select(shortcutId).executeAsOneOrNull() ?: return null

        if (c.intent.isEmpty()) {
            return null
        }

        val intent: Intent = try {
            Intent.parseUri(c.intent, 0)
        } catch (e: URISyntaxException) {
            return null
        }

        return Shortcut(shortcutId, c.iconType.toInt(), c.title, c.isCustomIcon == 1L, intent)
    }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    fun addItemToDatabase(item: Shortcut, icon: ShortcutIcon): Long {
        val values = createShortcutContentValues(item, icon)
        db.shortcutsQueries.insert(
                itemType = values.getAsLong(LauncherSettings.Favorites.ITEM_TYPE),
                title = values.getAsString(LauncherSettings.Favorites.TITLE),
                intent = values.getAsString(LauncherSettings.Favorites.INTENT),
                iconType = values.getAsLong(LauncherSettings.Favorites.ICON_TYPE),
                icon = values.getAsByteArray(LauncherSettings.Favorites.ICON),
                iconPackage = values.getAsString(LauncherSettings.Favorites.ICON_PACKAGE),
                iconResource = values.getAsString(LauncherSettings.Favorites.ICON_RESOURCE),
                isCustomIcon = values.getAsLong(LauncherSettings.Favorites.IS_CUSTOM_ICON)
        )
        return db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: Shortcut.idUnknown
    }

    /**
     * Removes the specified item from the database
     */
    fun deleteItemFromDatabase(shortcutId: Long) {
        db.shortcutsQueries.delete(shortcutId)
    }

    companion object {

        private fun getIconFromCursor(data: ByteArray, opts: BitmapFactory.Options): Bitmap? {
            if (data.isEmpty()) {
                return null
            }
            return try {
                BitmapFactory.decodeByteArray(data, 0, data.size, opts)
            } catch (e: Exception) {
                AppLog.e(e)
                null
            }
        }

        fun createShortcutContentValues(item: Shortcut, icon: ShortcutIcon): ContentValues {
            val values = ContentValues()
            values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType)
            values.put(LauncherSettings.Favorites.TITLE, item.title.toString())

            val uri = item.intent.toUri(0)
            values.put(LauncherSettings.Favorites.INTENT, uri)

            if (icon.isCustom) {
                values.put(LauncherSettings.Favorites.ICON_TYPE,
                        LauncherSettings.Favorites.ICON_TYPE_BITMAP)
                writeBitmap(values, icon.bitmap)
            } else {
                if (!icon.isFallback) {
                    writeBitmap(values, icon.bitmap)
                }
                values.put(LauncherSettings.Favorites.ICON_TYPE,
                        LauncherSettings.Favorites.ICON_TYPE_RESOURCE)
                if (icon.resource != null) {
                    values.put(LauncherSettings.Favorites.ICON_PACKAGE,
                            icon.resource.packageName)
                    values.put(LauncherSettings.Favorites.ICON_RESOURCE,
                            icon.resource.resourceName)
                }
            }
            values.put(LauncherSettings.Favorites.IS_CUSTOM_ICON, if (icon.isCustom) 1 else 0)
            return values
        }

        private fun writeBitmap(values: ContentValues, bitmap: Bitmap?) {
            if (bitmap != null) {
                val data = UtilitiesBitmap.flattenBitmap(bitmap)
                values.put(LauncherSettings.Favorites.ICON, data)
            }
        }
    }

}
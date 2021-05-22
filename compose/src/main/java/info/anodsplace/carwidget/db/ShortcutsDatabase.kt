package info.anodsplace.carwidget.db

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources.NotFoundException
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.carwidget.utils.UtilitiesBitmap
import info.anodsplace.carwidget.extentions.isLowMemoryDevice
import info.anodsplace.framework.AppLog
import java.net.URISyntaxException

class ShortcutsDatabase(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
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
        val c = contentResolver.query(shortcutUri, null, null, null, null)

        if (c == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        var shortcutIcon: ShortcutIcon? = null
        try {
            c.moveToFirst()

            val idIndex = c.getColumnIndexOrThrow(BaseColumns._ID)
            val iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE)
            val iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON)
            val iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE)
            val iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE)
            val itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE)
            val isCustomIconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON)

            val id = c.getLong(idIndex)
            val itemType = c.getInt(itemTypeIndex)

            var icon: Bitmap? = null
            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                icon = getIconFromCursor(c, iconIndex, bitmapOptions)
                shortcutIcon = if (c.getInt(isCustomIconIndex) == 1) {
                    ShortcutIcon.forCustomIcon(id, icon!!)
                } else {
                    ShortcutIcon.forActivity(id, icon!!)
                }
            } else {
                val iconType = c.getInt(iconTypeIndex)
                if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
                    val packageName = c.getString(iconPackageIndex)
                    val resourceName = c.getString(iconResourceIndex)
                    val iconResource = Intent.ShortcutIconResource()
                    iconResource.packageName = packageName
                    iconResource.resourceName = resourceName
                    // the resource
                    try {
                        val resources = packageManager.getResourcesForApplication(packageName)
                        val resId = resources.getIdentifier(resourceName, null, null)
                        if (resId > 0) {
                            val iconDrawable = ResourcesCompat.getDrawable(resources, resId, null)!!
                            icon = UtilitiesBitmap.createHiResIconBitmap(iconDrawable, context)
                        }
                    } catch (e: NameNotFoundException) {
                        // drop this. we have other places to look for icons
                        AppLog.w("loadShortcutIcon: NameNotFoundException $packageName/$resourceName")
                    } catch (e: NotFoundException) {
                        AppLog.w("loadShortcutIcon: NotFoundException $packageName/$resourceName")
                    }

                    // the db
                    if (icon == null) {
                        icon = getIconFromCursor(c, iconIndex, bitmapOptions)
                    }
                    shortcutIcon = ShortcutIcon.forIconResource(id, icon!!, iconResource)
                } else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
                    icon = getIconFromCursor(c, iconIndex, bitmapOptions)
                    if (icon != null) {
                        shortcutIcon = ShortcutIcon.forCustomIcon(id, icon)
                    }
                }
            }

        } catch (e: Exception) {
            AppLog.e(e)
        } finally {
            c.close()
        }

        if (shortcutIcon == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        return shortcutIcon
    }

    fun loadShortcut(shortcutId: Long): Shortcut? {
        val selection = BaseColumns._ID + "=?"
        val selectionArgs = arrayOf(shortcutId.toString())
        var info: Shortcut?
        val cursor = contentResolver.query(
                LauncherSettings.Favorites.getContentUri(context.packageName), null,
                        selection, selectionArgs, null) ?: return null

        cursor.use { c ->
            val intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT)
            c.moveToFirst()
            val intent: Intent
            val intentDescription = c.getString(intentIndex)
            if (TextUtils.isEmpty(intentDescription)) {
                return null
            }
            try {
                intent = Intent.parseUri(intentDescription, 0)
            } catch (e: URISyntaxException) {
                c.close()
                return null
            }

            val idIndex = c.getColumnIndexOrThrow(BaseColumns._ID)
            val titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)
            val itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE)
            val isCustomIconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON)

            val id = c.getLong(idIndex)
            val title = c.getString(titleIndex) ?: ""
            val itemType = c.getInt(itemTypeIndex)
            val isCustomIcon = c.getInt(isCustomIconIndex) == 1

            info = Shortcut(id, itemType, title, isCustomIcon, intent)
        }

        return info
    }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    fun addItemToDatabase(context: Context, item: Shortcut, icon: ShortcutIcon): Long {

        val cr = context.contentResolver
        val values = createShortcutContentValues(item, icon)

        val result = cr
                .insert(LauncherSettings.Favorites.getContentUri(context.packageName), values)

        return if (result != null) {
            Integer.parseInt(result.pathSegments[1]).toLong()
        } else Shortcut.idUnknown
    }

    /**
     * Update an item to the database in a specified container.
     */
    fun updateItemInDatabase(context: Context, item: Shortcut, icon: ShortcutIcon) {
        val cr = context.contentResolver

        val values = createShortcutContentValues(item, icon)

        cr.update(LauncherSettings.Favorites.getContentUri(context.packageName, item.id),
                values, null, null)
    }

    /**
     * Removes the specified item from the database
     */
    fun deleteItemFromDatabase(shortcutId: Long) {
        val uriToDelete = LauncherSettings.Favorites
                .getContentUri(context.packageName, shortcutId)
        Runnable { contentResolver.delete(uriToDelete, null, null) }
    }

    companion object {

        private fun getIconFromCursor(c: Cursor, iconIndex: Int, opts: BitmapFactory.Options): Bitmap? {
            val data = c.getBlob(iconIndex)
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
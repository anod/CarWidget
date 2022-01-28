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
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Database
import info.anodsplace.carwidget.content.extentions.isLowMemoryDevice
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
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

    suspend fun loadShortcutIcon(shortcutUri: Uri): ShortcutIcon = withContext(Dispatchers.IO) {
        val id = shortcutUri.lastPathSegment!!.toLong()
        val c = db.shortcutsQueries.selectIcon(id).executeAsOneOrNull()

        if (c == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return@withContext ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
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
            return@withContext ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        return@withContext shortcutIcon
    }

    fun observeShortcut(shortcutId: Long): Flow<Shortcut> {
        return db.shortcutsQueries.select(shortcutId).asFlow()
                .mapToOne()
                .mapNotNull { mapShortcut(shortcutId, it) }
    }

    suspend fun loadShortcut(shortcutId: Long): Shortcut? = withContext(Dispatchers.IO) {
        val c = db.shortcutsQueries.select(shortcutId).executeAsOneOrNull() ?: return@withContext null
        return@withContext mapShortcut(shortcutId, c)
    }

    private fun mapShortcut(shortcutId: Long, c: Favorites): Shortcut? {
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
    suspend fun addItemToDatabase(item: Shortcut, icon: ShortcutIcon): Long = withContext(Dispatchers.IO) {
        val values = createShortcutContentValues(item, icon)
        return@withContext db.transactionWithResult {
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
            return@transactionWithResult db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: Shortcut.idUnknown
        }
    }

    /**
     * Removes the specified item from the database
     */
    suspend fun deleteItemFromDatabase(shortcutId: Long) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.delete(shortcutId)
    }

    suspend fun updateIntent(shortcutId: Long, intent: Intent) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.updateIntent(
                intent = intent.toUri(0),
                _id = shortcutId
        )
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

        suspend fun loadIconFromDatabase(shortcutId: Long, context: Context, db: ShortcutsDatabase): ShortcutIcon {
            val shortcutUri = LauncherSettings.Favorites.getContentUri(context.packageName, shortcutId)
            return db.loadShortcutIcon(shortcutUri)
        }

        private fun writeBitmap(values: ContentValues, bitmap: Bitmap?) {
            if (bitmap != null) {
                val data = UtilitiesBitmap.flattenBitmap(bitmap)
                values.put(LauncherSettings.Favorites.ICON, data)
            }
        }
    }

}
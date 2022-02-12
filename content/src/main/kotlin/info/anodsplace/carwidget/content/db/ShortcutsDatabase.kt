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
import android.util.SparseArray
import androidx.core.content.res.ResourcesCompat
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.Database
import info.anodsplace.carwidget.content.extentions.isLowMemoryDevice
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.net.URISyntaxException
import java.util.ArrayList

typealias ShortcutWithIcon = Pair<Shortcut, ShortcutIcon?>

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
        val c = db.shortcutsQueries.selectShortcutIcon(id).executeAsOneOrNull()

        if (c == null) {
            val icon = UtilitiesBitmap.makeDefaultIcon(packageManager)
            return@withContext ShortcutIcon.forFallbackIcon(shortcutUri.lastPathSegment!!.toLong(), icon)
        }

        var shortcutIcon: ShortcutIcon? = null
        try {
            val itemType = c.itemType

            var icon: Bitmap? = null
            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                icon = getIconFromCursor(c.icon ?: ByteArray(0), bitmapOptions)
                if (icon != null) {
                    shortcutIcon = if (c.isCustomIcon) {
                        ShortcutIcon.forCustomIcon(id, icon)
                    } else {
                        ShortcutIcon.forActivity(id, icon)
                    }
                }
            } else {
                val iconType = c.iconType
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

    fun observeShortcut(shortcutId: Long): Flow<Shortcut?> {
        return db.shortcutsQueries.selectShortcut(shortcutId, mapper = ::mapShortcut).asFlow()
                .mapToOne()
                .filter { it.isValid }
    }

    fun observeTarget(targetId: Int): Flow<Map<Int, Shortcut?>> {
        return db.shortcutsQueries.selectTarget(targetId, mapper = ::mapShortcut).asFlow()
                .mapToList()
                .map { list ->
                    list.associate { sh -> sh.position to if (sh.isValid) sh else null }
                }
    }

    suspend fun loadShortcut(shortcutId: Long): Shortcut? = withContext(Dispatchers.IO) {
        val shortcut = db.shortcutsQueries.selectShortcut(shortcutId, mapper = ::mapShortcut).executeAsOneOrNull()
        if (shortcut?.isValid == true) {
            return@withContext shortcut
        }
        return@withContext null
    }

    suspend fun loadTarget(targetId: Int): Map<Int, Shortcut?> = withContext(Dispatchers.IO) {
        val list = db.shortcutsQueries.selectTarget(targetId, mapper = ::mapShortcut).executeAsList()
        return@withContext list.associate { sh -> sh.position to if (sh.isValid) sh else null }
    }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    suspend fun addItem(targetId: Int, position: Int, item: Shortcut, icon: ShortcutIcon): Long = withContext(Dispatchers.IO) {
        insert(targetId, position, item, icon)
        return@withContext db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: Shortcut.idUnknown
    }

    /**
     * Removes the specified item from the database
     */
    suspend fun deleteShortcut(shortcutId: Long) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.deleteShortcut(shortcutId)
    }

    suspend fun deleteTargets(targetIds: List<Int>) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.deleteTargets(targetIds)
    }

    suspend fun updateIntent(shortcutId: Long, intent: Intent) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.updateShortcutIntent(
                intent = intent.toUri(0),
                shortcutId = shortcutId
        )
    }

    suspend fun restoreTarget(targetId: Int, items: SparseArray<ShortcutWithIcon>) = withContext(Dispatchers.IO) {
        db.transaction {
            db.shortcutsQueries.deleteTarget(targetId)

            for (position in 0 until items.size()) {
                val item = items.get(position)
                if (item?.first != null) {
                    insert(targetId, position, item.first, item.second!!)
                }
            }
        }
    }

    private fun insert(targetId: Int, position: Int, item: Shortcut, icon: ShortcutIcon)  {
        val values = createShortcutContentValues(item, icon)
        db.shortcutsQueries.insert(
                targetId = targetId,
                position = position,
                itemType = values.getAsInteger(LauncherSettings.Favorites.ITEM_TYPE),
                title = values.getAsString(LauncherSettings.Favorites.TITLE),
                intent = values.getAsString(LauncherSettings.Favorites.INTENT),
                iconType = values.getAsInteger(LauncherSettings.Favorites.ICON_TYPE),
                icon = values.getAsByteArray(LauncherSettings.Favorites.ICON),
                iconPackage = values.getAsString(LauncherSettings.Favorites.ICON_PACKAGE),
                iconResource = values.getAsString(LauncherSettings.Favorites.ICON_RESOURCE),
                isCustomIcon = values.getAsBoolean(LauncherSettings.Favorites.IS_CUSTOM_ICON)
        )
    }

    suspend fun deleteTargetPosition(targetId: Int, position: Int) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.deleteTargetPosition(targetId, position)
    }

    suspend fun moveShortcut(targetId: Int, from: Int, to: Int) = withContext(Dispatchers.IO) {
        db.transaction {
            db.shortcutsQueries.updateTargetPosition(targetId = targetId, position = from, newPosition = -1)
            db.shortcutsQueries.updateTargetPosition(targetId = targetId, position = to, newPosition = from)
            db.shortcutsQueries.updateTargetPosition(targetId = targetId, position = -1, newPosition = to)
        }
    }

    suspend fun migrateShortcutPosition(targetId: Int, positionIds: ArrayList<Long>) = withContext(Dispatchers.IO) {
        db.transaction {
            positionIds.forEachIndexed { index, shortcutId ->
                if (shortcutId != Shortcut.idUnknown) {
                    db.shortcutsQueries.migrateShortcutPosition(shortcutId = shortcutId, targetId = targetId, position = index)
                }
            }
        }
    }

    private fun mapShortcut(shortcutId: Long, position: Int, title: String, itemType: Int, intent: String, isCustomIcon: Boolean): Shortcut {
        if (intent.isEmpty()) {
            return Shortcut.unknown
        }

        val parsedIntent: Intent = try {
            Intent.parseUri(intent, 0)
        } catch (e: URISyntaxException) {
            return Shortcut.unknown
        }

        return Shortcut(shortcutId, position, itemType, title, isCustomIcon, parsedIntent)
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
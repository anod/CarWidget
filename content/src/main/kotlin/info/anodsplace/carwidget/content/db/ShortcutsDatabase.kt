package info.anodsplace.carwidget.content.db

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.util.SparseArray
import androidx.core.util.size
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URISyntaxException

typealias ShortcutWithIcon = Pair<Shortcut, ShortcutIcon>
typealias ShortcutWithFolderItems = Triple<Shortcut, ShortcutIcon, List<ShortcutWithIcon>?>

class ShortcutsDatabase(private val db: Database) {

    fun observeChanges(): Flow<Long> {
        return db.shortcutsQueries.changes().asFlow().mapToOneOrNull(Dispatchers.IO).filterNotNull()
    }

    suspend fun loadIcon(shortcutId: Long): SelectShortcutIcon? = withContext(Dispatchers.IO) {
        return@withContext db.shortcutsQueries.selectShortcutIcon(shortcutId).executeAsOneOrNull()
    }

    fun observeIcon(shortcutId: Long): Flow<SelectShortcutIcon?> {
        return db.shortcutsQueries.selectShortcutIcon(shortcutId).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    fun observeShortcut(shortcutId: Long): Flow<Shortcut?> {
        return db.shortcutsQueries.selectShortcut(shortcutId, mapper = ::mapShortcut).asFlow()
                .mapToOneOrNull(Dispatchers.IO)
                .filter { it?.isValid == true }
    }

    fun observeTarget(targetId: Int): Flow<Map<Int, Shortcut?>> {
        return db.shortcutsQueries.selectTarget(targetId, mapper = ::mapShortcut).asFlow()
                .mapToList(Dispatchers.IO)
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

    suspend fun loadShortcut(targetId: Int, position: Int): Shortcut? = withContext(Dispatchers.IO) {
        val shortcut = db.shortcutsQueries.selectTargetPosition(targetId, position, mapper = ::mapShortcut).executeAsOneOrNull()
        if (shortcut?.isValid == true) {
            return@withContext shortcut
        }
        return@withContext null
    }

    suspend fun loadFolderItems(shortcutId: Long): List<Shortcut> = withContext(Dispatchers.IO) {
        val list = db.folderItemQueries.selectFolderShortcut(shortcutId, mapper = ::mapFolderItem).executeAsList()
        return@withContext list.filter { it.isValid }
    }

    suspend fun loadFolderIcon(folderId: Long): SelectFolderShortcutIcon? = withContext(Dispatchers.IO) {
        return@withContext db.folderItemQueries.selectFolderShortcutIcon(folderId).executeAsOneOrNull()
    }

    fun observeFolder(shortcutId: Long): Flow<List<Shortcut>> {
        return db.folderItemQueries.selectFolderShortcut(shortcutId, mapper = ::mapFolderItem).asFlow()
            .mapToList(Dispatchers.IO)
    }

    /**
     * Add an item to the database in a specified container. Sets the container,
     * screen, cellX and cellY fields of the item. Also assigns an ID to the
     * item.
     */
    suspend fun addItem(targetId: Int, position: Int, item: Shortcut, icon: ShortcutIcon): Long = withContext(Dispatchers.IO) {
        insert(targetId, position, item, icon)
        return@withContext db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: Shortcut.ID_UNKNOWN
    }

    suspend fun saveFolder(targetId: Int, position: Int, item: Shortcut, icon: ShortcutIcon, items: List<Pair<Shortcut, ShortcutIcon>>): Long = withContext(Dispatchers.IO) {
        return@withContext db.transactionWithResult {
            insert(targetId, position, item, icon)
            val shortcutId = db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: return@transactionWithResult Shortcut.ID_UNKNOWN
            items.forEach { (shortcut, icon) ->
                insertFolderItem(shortcutId, shortcut, icon)
            }
            shortcutId
        }
    }

    suspend fun updateFolder(shortcutId: Long, items: List<Pair<Shortcut, ShortcutIcon>>): Long = withContext(Dispatchers.IO) {
        return@withContext db.transactionWithResult {
            db.folderItemQueries.deleteFolderTarget(shortcutId)
            items.forEach { (shortcut, icon) ->
                insertFolderItem(shortcutId, shortcut, icon)
            }
            shortcutId
        }
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

    suspend fun updateIcon(shortcutId: Long, icon: ShortcutIcon) = withContext(Dispatchers.IO) {
        val values = ContentValues().also {
            it.putIcon(icon)
        }
        db.shortcutsQueries.updateShortcutIcon(
            iconType = values.getAsInteger(LauncherSettings.Favorites.ICON_TYPE),
            icon = values.getAsByteArray(LauncherSettings.Favorites.ICON),
            iconPackage = values.getAsString(LauncherSettings.Favorites.ICON_PACKAGE),
            iconResource = values.getAsString(LauncherSettings.Favorites.ICON_RESOURCE),
            isCustomIcon = values.getAsBoolean(LauncherSettings.Favorites.IS_CUSTOM_ICON),
            shortcutId = shortcutId
        )
    }

    suspend fun updateTitle(shortcutId: Long, title: String) = withContext(Dispatchers.IO) {
        db.shortcutsQueries.updateShortcutTitle(title = title, shortcutId = shortcutId)
    }

    suspend fun restoreTarget(targetId: Int, items: SparseArray<ShortcutWithFolderItems>) = withContext(Dispatchers.IO) {
        db.transaction {
            db.shortcutsQueries.deleteTarget(targetId)
            db.folderItemQueries.deleteFolderTarget(targetId.toLong())

            for (position in 0 until items.size) {
                val item = items.get(position)
                val shortcut = item?.first ?: continue
                insert(targetId, position, item.first, item.second!!)
                if (shortcut.isFolder) {
                    val shortcutId = db.shortcutsQueries.lastInsertId().executeAsOneOrNull() ?: continue
                    val folderItems = item.third
                    folderItems?.forEach { (folderShortcut, folderIcon) ->
                        insertFolderItem(shortcutId, folderShortcut, folderIcon)
                    }
                }
            }
        }
    }

    private fun insert(targetId: Int, position: Int, item: Shortcut, icon: ShortcutIcon)  {
        val values = createShortcutContentValues(item, icon)
        try {
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
        } catch (e: SQLiteConstraintException) {
            AppLog.e(e)
            db.shortcutsQueries.update(
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
    }

    private fun insertFolderItem(shortcutId: Long, item: Shortcut, icon: ShortcutIcon) {
        val values = createShortcutContentValues(item, icon)
        try {
            db.folderItemQueries.insertFolder(
                shortcutId = shortcutId,
                itemId = item.itemId,
                itemType = values.getAsInteger(LauncherSettings.Favorites.ITEM_TYPE),
                title = values.getAsString(LauncherSettings.Favorites.TITLE),
                intent = values.getAsString(LauncherSettings.Favorites.INTENT),
                iconType = values.getAsInteger(LauncherSettings.Favorites.ICON_TYPE),
                icon = values.getAsByteArray(LauncherSettings.Favorites.ICON),
                iconPackage = values.getAsString(LauncherSettings.Favorites.ICON_PACKAGE),
                iconResource = values.getAsString(LauncherSettings.Favorites.ICON_RESOURCE),
                isCustomIcon = values.getAsBoolean(LauncherSettings.Favorites.IS_CUSTOM_ICON)
            )
        } catch (e: SQLiteConstraintException) {
            AppLog.e(e)
            db.folderItemQueries.updateFolder(
                shortcutId = shortcutId,
                itemId = item.itemId,
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
                if (shortcutId != Shortcut.ID_UNKNOWN) {
                    db.shortcutsQueries.migrateShortcutPosition(shortcutId = shortcutId, targetId = targetId, position = index)
                }
            }
        }
    }

    private fun mapShortcut(shortcutId: Long, position: Int, title: String, itemType: Int, intent: String, isCustomIcon: Boolean): Shortcut {
        if (intent.isEmpty()) {
            AppLog.e("Intent is empty for id:${shortcutId} title:${title}")
            Shortcut(shortcutId, position, itemType, title, isCustomIcon, Intent())
        }

        val parsedIntent: Intent = try {
            Intent.parseUri(intent, 0)
        } catch (e: URISyntaxException) {
            AppLog.e(e)
            Intent()
        }

        return Shortcut(shortcutId, position, itemType, title, isCustomIcon, parsedIntent)
    }

    private fun mapFolderItem(
        id: Long,
        shortcutId: Long,
        itemId: String,
        title: String,
        itemType: Int,
        intent: String,
        isCustomIcon: Boolean
    ): Shortcut {
        if (intent.isEmpty()) {
            AppLog.e("Intent is empty for id:${itemId} title:${title}")
            return Shortcut(id, -1, itemType, title, isCustomIcon, Intent())
        }

        val parsedIntent: Intent = try {
            Intent.parseUri(intent, 0)
        } catch (e: URISyntaxException) {
            AppLog.e(e)
            Intent()
        }

        return Shortcut(id, -1, itemType, title, isCustomIcon, parsedIntent)
    }

    companion object {

        fun createShortcutContentValues(item: Shortcut, icon: ShortcutIcon): ContentValues {
            val values = ContentValues()
            values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType)
            values.put(LauncherSettings.Favorites.TITLE, item.title.toString())

            val uri = item.intent.toUri(0)
            values.put(LauncherSettings.Favorites.INTENT, uri)
            values.putIcon(icon)
            return values
        }

        private fun ContentValues.putIcon(icon: ShortcutIcon) {
            if (icon.isCustom) {
                put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_BITMAP)
                writeBitmap(this, icon.bitmap)
            } else {
                if (!icon.isFallback) {
                    writeBitmap(this, icon.bitmap)
                }
                put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_RESOURCE)
                if (icon.resource != null) {
                    put(LauncherSettings.Favorites.ICON_PACKAGE,
                        icon.resource.packageName)
                    put(LauncherSettings.Favorites.ICON_RESOURCE,
                        icon.resource.resourceName)
                }
            }
            put(LauncherSettings.Favorites.IS_CUSTOM_ICON, if (icon.isCustom) 1 else 0)
        }

        private fun writeBitmap(values: ContentValues, bitmap: Bitmap?) {
            if (bitmap != null) {
                val data = UtilitiesBitmap.flattenBitmap(bitmap)
                values.put(LauncherSettings.Favorites.ICON, data)
            }
        }
    }
}
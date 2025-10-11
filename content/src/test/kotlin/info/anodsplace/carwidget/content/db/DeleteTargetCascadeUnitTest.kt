package info.anodsplace.carwidget.content.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies ON DELETE CASCADE works when deleting favorites via deleteTargetPosition and deleteTarget.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class DeleteTargetCascadeUnitTest {
    private val intAsLongAdapter = object : ColumnAdapter<Int, Long> {
        override fun decode(databaseValue: Long): Int = databaseValue.toInt()
        override fun encode(value: Int): Long = value.toLong()
    }

    private fun createDb(): Pair<Database, AndroidSqliteDriver> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(Database.Schema, context, name = "delete-target-cascade.db", callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) {
                db.setForeignKeyConstraintsEnabled(true)
            }
        })
        val db = Database(
            driver = driver,
            FolderItemAdapter = FolderItem.Adapter(
                itemTypeAdapter = intAsLongAdapter,
                iconTypeAdapter = intAsLongAdapter
            ),
            favoritesAdapter = Favorites.Adapter(
                targetIdAdapter = intAsLongAdapter,
                positionAdapter = intAsLongAdapter,
                itemTypeAdapter = intAsLongAdapter,
                iconTypeAdapter = intAsLongAdapter
            )
        )
        return db to driver
    }

    private fun Database.insertFavorite(targetId: Int, position: Int, title: String = "Fav", itemType: Int = 0): Long {
        shortcutsQueries.insert(
            targetId = targetId,
            position = position,
            itemType = itemType,
            title = title,
            intent = "",
            iconType = 0,
            icon = null,
            iconPackage = null,
            iconResource = null,
            isCustomIcon = false
        )
        return shortcutsQueries.lastInsertId().executeAsOne()
    }

    private fun Database.insertFolderItem(shortcutId: Long, itemId: String = "item1", title: String = "Item", itemType: Int = 0) {
        folderItemQueries.insertFolder(
            shortcutId = shortcutId,
            itemId = itemId,
            itemType = itemType,
            title = title,
            intent = "",
            iconType = 0,
            icon = null,
            iconPackage = null,
            iconResource = null,
            isCustomIcon = false
        )
    }

    @Test
    fun cascadeDelete_viaDeleteTargetPosition() {
        val (db, driver) = createDb()
        driver.use {
            val targetId = 42
            val position = 0
            val favId = db.insertFavorite(targetId = targetId, position = position)
            db.insertFolderItem(shortcutId = favId)
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(favId).executeAsList().size)
            // Delete by target + position
            db.shortcutsQueries.deleteTargetPosition(targetId, position)
            // Folder items should be gone due to ON DELETE CASCADE
            assertEquals(0, db.folderItemQueries.selectFolderShortcut(favId).executeAsList().size)
        }
    }

    @Test
    fun cascadeDelete_viaDeleteTarget() {
        val (db, driver) = createDb()
        driver.use {
            val targetId = 99
            val fav1 = db.insertFavorite(targetId = targetId, position = 0, title = "Fav1")
            val fav2 = db.insertFavorite(targetId = targetId, position = 1, title = "Fav2")
            db.insertFolderItem(shortcutId = fav1, itemId = "i1")
            db.insertFolderItem(shortcutId = fav2, itemId = "i2")
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(fav1).executeAsList().size)
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(fav2).executeAsList().size)
            // Delete all favorites for target
            db.shortcutsQueries.deleteTarget(targetId)
            // All folder items referencing deleted favorites should be removed
            assertEquals(0, db.folderItemQueries.selectFolderShortcut(fav1).executeAsList().size)
            assertEquals(0, db.folderItemQueries.selectFolderShortcut(fav2).executeAsList().size)
        }
    }
}


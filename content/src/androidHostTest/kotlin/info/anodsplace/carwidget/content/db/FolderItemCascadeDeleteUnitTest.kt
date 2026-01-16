package info.anodsplace.carwidget.content.db

import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric migration of instrumentation FolderItemCascadeDeleteTest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class FolderItemCascadeDeleteUnitTest {
    private val intAsLongAdapter = object : ColumnAdapter<Int, Long> {
        override fun decode(databaseValue: Long): Int = databaseValue.toInt()
        override fun encode(value: Int): Long = value.toLong()
    }

    private fun createDb(): Pair<Database, AndroidSqliteDriver> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(Database.Schema, context, name = "cascade-delete-unit.db")
        driver.execute(null, "PRAGMA foreign_keys=ON", 0)
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

    private fun Database.insertFavorite(position: Int = 0, title: String = "Fav", itemType: Int = 0): Long {
        shortcutsQueries.insert(
            targetId = 0,
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
    fun cascadeDelete_singleFavorite() {
        val (db, driver) = createDb()
        driver.use {
            val favId = db.insertFavorite()
            db.insertFolderItem(shortcutId = favId)
            val before = db.folderItemQueries.selectFolderShortcut(favId).executeAsList()
            assertEquals(1, before.size)
            db.shortcutsQueries.deleteShortcut(favId)
            val after = db.folderItemQueries.selectFolderShortcut(favId).executeAsList()
            assertEquals(0, after.size)
        }
    }

    @Test
    fun cascadeDelete_onlyDeletesAssociatedFolderItems() {
        val (db, driver) = createDb()
        driver.use {
            val favId1 = db.insertFavorite(position = 1, title = "Fav1")
            val favId2 = db.insertFavorite(position = 2, title = "Fav2")
            db.insertFolderItem(shortcutId = favId1, itemId = "i1")
            db.insertFolderItem(shortcutId = favId2, itemId = "i2")
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(favId1).executeAsList().size)
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(favId2).executeAsList().size)
            db.shortcutsQueries.deleteShortcut(favId1)
            assertEquals(0, db.folderItemQueries.selectFolderShortcut(favId1).executeAsList().size)
            assertEquals(1, db.folderItemQueries.selectFolderShortcut(favId2).executeAsList().size)
        }
    }
}


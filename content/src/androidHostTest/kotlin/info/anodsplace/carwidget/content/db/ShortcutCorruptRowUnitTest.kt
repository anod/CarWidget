// Copyright (c) CarWidget contributors. Licensed under the project license.
package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.content.Intent
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Corrupt rows (empty/unparseable intent) must be treated as invalid so that reads suppress
 * them: loadShortcut/loadTarget return null for the slot, and folder reads (loadFolderItems,
 * observeFolder) drop the bad child while keeping the valid ones.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ShortcutCorruptRowUnitTest {

    private fun createDb(): Triple<ShortcutsDatabase, Database, AndroidSqliteDriver> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "shortcut-corrupt-unit.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            }
        )
        val database = Database(
            driver = driver,
            favoritesAdapter = Favorites.Adapter(
                targetIdAdapter = IntColumnAdapter,
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
                positionAdapter = IntColumnAdapter
            ),
            FolderItemAdapter = FolderItem.Adapter(
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter
            )
        )
        return Triple(ShortcutsDatabase(database), database, driver)
    }

    private fun activityIntentUri(cls: String): String =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName("com.test", cls))
            .toUri(0)

    private fun Database.insertRawShortcut(targetId: Int, position: Int, title: String, intent: String) {
        shortcutsQueries.insert(
            targetId = targetId,
            position = position,
            itemType = 0,
            title = title,
            intent = intent,
            iconType = 0,
            icon = null,
            iconPackage = null,
            iconResource = null,
            isCustomIcon = false
        )
    }

    private fun Database.insertRawFolderItem(shortcutId: Long, itemId: String, title: String, intent: String) {
        folderItemQueries.insertFolder(
            shortcutId = shortcutId,
            itemId = itemId,
            itemType = 0,
            title = title,
            intent = intent,
            iconType = 0,
            icon = null,
            iconPackage = null,
            iconResource = null,
            isCustomIcon = false
        )
    }

    @Test
    fun emptyIntentShortcut_isSuppressedButValidNeighbourRemains() = runBlocking<Unit> {
        val (shortcutsDb, database, driver) = createDb()
        driver.use {
            database.insertRawShortcut(TARGET, 0, "Corrupt", intent = "")
            database.insertRawShortcut(TARGET, 1, "Valid", intent = activityIntentUri("ValidActivity"))

            assertNull(shortcutsDb.loadShortcut(TARGET, 0))
            assertNotNull(shortcutsDb.loadShortcut(TARGET, 1))

            val target = shortcutsDb.loadTarget(TARGET)
            assertNull(target[0])
            assertEquals("Valid", target[1]?.title)
        }
    }

    @Test
    fun emptyIntentFolderItem_isFilteredFromFolderReads() = runBlocking<Unit> {
        val (shortcutsDb, database, driver) = createDb()
        driver.use {
            database.insertRawShortcut(TARGET, 0, "Folder", intent = activityIntentUri("FolderActivity"))
            val folderId = database.shortcutsQueries.lastInsertId().executeAsOne()
            database.insertRawFolderItem(folderId, itemId = "valid", title = "Child", intent = activityIntentUri("ChildActivity"))
            database.insertRawFolderItem(folderId, itemId = "corrupt", title = "Broken", intent = "")

            val items = shortcutsDb.loadFolderItems(folderId)
            assertEquals(1, items.size)
            assertEquals("Child", items[0].title)

            val observed = shortcutsDb.observeFolder(folderId).first()
            assertEquals(1, observed.size)
            assertEquals("Child", observed[0].title)
        }
    }

    companion object {
        private const val TARGET = 33
    }
}

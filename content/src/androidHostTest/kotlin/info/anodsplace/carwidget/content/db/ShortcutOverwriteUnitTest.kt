// Copyright (c) CarWidget contributors. Licensed under the project license.
package info.anodsplace.carwidget.content.db

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers the delete-then-insert write path: overwriting an occupied (targetId, position)
 * slot must attach folder children to the freshly inserted row, cascade-delete the previous
 * row's children, and let copyShortcut replace an occupied slot without throwing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ShortcutOverwriteUnitTest {

    private fun createDb(): Pair<ShortcutsDatabase, AndroidSqliteDriver> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "shortcut-overwrite-unit.db",
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
        return ShortcutsDatabase(database) to driver
    }

    private fun icon() = ShortcutIcon.forActivity(0, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    private fun activity(title: String, cls: String): Shortcut =
        Shortcut.forActivity(0, 0, title, false, ComponentName("com.test", cls), 0)

    private fun folder(title: String): Shortcut =
        Shortcut(
            id = 0,
            position = 0,
            itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION,
            title = title,
            isCustomIcon = false,
            intent = Intent(ShortcutExtra.ACTION_FOLDER)
        )

    @Test
    fun saveFolder_overOccupiedPosition_attachesChildrenToNewFolder() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            // Occupy the destination slot, then create another shortcut so last_insert_rowid()
            // points at a different row than the slot being overwritten.
            db.addItem(TARGET, 2, activity("Occupant", "OccupantActivity"), icon())
            val neighbourId = db.addItem(TARGET, 0, activity("Neighbour", "NeighbourActivity"), icon())

            val children = listOf(
                activity("Child1", "Child1Activity") to icon(),
                activity("Child2", "Child2Activity") to icon()
            )
            val folderId = db.saveFolder(TARGET, 2, folder("Folder"), icon(), children)

            val folderRow = db.loadShortcut(TARGET, 2)
            assertNotNull(folderRow)
            assertTrue(folderRow!!.isFolder)
            assertEquals(folderRow.id, folderId)

            val folderItems = db.loadFolderItems(folderId)
            assertEquals(2, folderItems.size)
            assertEquals("Child1", folderItems[0].title)
            assertEquals("Child2", folderItems[1].title)

            // Children must not be misattached to the unrelated neighbour row.
            assertTrue(db.loadFolderItems(neighbourId).isEmpty())
        }
    }

    @Test
    fun saveFolder_overExistingFolder_cascadeDeletesOldChildren() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            val oldId = db.saveFolder(
                TARGET, 0, folder("Old"), icon(),
                listOf(
                    activity("OldA", "OldAActivity") to icon(),
                    activity("OldB", "OldBActivity") to icon()
                )
            )
            assertEquals(2, db.loadFolderItems(oldId).size)

            val newId = db.saveFolder(
                TARGET, 0, folder("New"), icon(),
                listOf(activity("NewA", "NewAActivity") to icon())
            )

            assertNotEqualsId(oldId, newId)
            assertTrue(db.loadFolderItems(oldId).isEmpty())
            val newItems = db.loadFolderItems(newId)
            assertEquals(1, newItems.size)
            assertEquals("NewA", newItems[0].title)
            assertEquals(newId, db.loadShortcut(TARGET, 0)!!.id)
        }
    }

    @Test
    fun copyShortcut_ontoOccupiedPosition_replacesWithoutThrowing() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            val sourceId = db.addItem(TARGET, 0, activity("Source", "SourceActivity"), icon())
            db.addItem(TARGET, 1, activity("Occupant", "OccupantActivity"), icon())

            val result = db.copyShortcut(TARGET, 1, sourceId)

            assertTrue(result)
            val copied = db.loadShortcut(TARGET, 1)
            assertNotNull(copied)
            assertEquals("Source", copied!!.title)
            assertEquals(2, db.loadTarget(TARGET).size)
        }
    }

    @Test
    fun copyShortcut_folderOntoOccupiedPosition_copiesChildrenAndDropsOld() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            val sourceId = db.saveFolder(
                TARGET, 0, folder("Source"), icon(),
                listOf(
                    activity("SrcA", "SrcAActivity") to icon(),
                    activity("SrcB", "SrcBActivity") to icon()
                )
            )
            val destId = db.saveFolder(
                TARGET, 1, folder("Dest"), icon(),
                listOf(activity("OldChild", "OldChildActivity") to icon())
            )

            val result = db.copyShortcut(TARGET, 1, sourceId)

            assertTrue(result)
            val newDest = db.loadShortcut(TARGET, 1)
            assertNotNull(newDest)
            assertTrue(newDest!!.isFolder)
            val copiedItems = db.loadFolderItems(newDest.id)
            assertEquals(2, copiedItems.size)
            assertEquals("SrcA", copiedItems[0].title)
            assertEquals("SrcB", copiedItems[1].title)
            assertTrue(db.loadFolderItems(destId).isEmpty())
        }
    }

    @Test
    fun copyShortcut_ontoOwnPosition_isNoOpAndKeepsSource() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            val sourceId = db.addItem(TARGET, 1, activity("Source", "SourceActivity"), icon())

            // Copying a shortcut onto the slot it already occupies must not destroy it.
            val result = db.copyShortcut(TARGET, 1, sourceId)

            assertTrue(result)
            val kept = db.loadShortcut(TARGET, 1)
            assertNotNull(kept)
            assertEquals(sourceId, kept!!.id)
            assertEquals("Source", kept.title)
            assertEquals(1, db.loadTarget(TARGET).size)
        }
    }

    @Test
    fun copyShortcut_folderOntoOwnPosition_keepsChildren() = runBlocking<Unit> {
        val (db, driver) = createDb()
        driver.use {
            val folderId = db.saveFolder(
                TARGET, 1, folder("Source"), icon(),
                listOf(
                    activity("SrcA", "SrcAActivity") to icon(),
                    activity("SrcB", "SrcBActivity") to icon()
                )
            )

            val result = db.copyShortcut(TARGET, 1, folderId)

            assertTrue(result)
            val kept = db.loadShortcut(TARGET, 1)
            assertNotNull(kept)
            assertEquals(folderId, kept!!.id)
            val items = db.loadFolderItems(folderId)
            assertEquals(2, items.size)
            assertEquals("SrcA", items[0].title)
            assertEquals("SrcB", items[1].title)
        }
    }

    private fun assertNotEqualsId(unexpected: Long, actual: Long) =
        assertFalse("expected a new row id, got reused id $actual", unexpected == actual)

    companion object {
        private const val TARGET = 21
    }
}

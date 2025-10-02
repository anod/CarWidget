package info.anodsplace.carwidget.content.backup

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.util.JsonReader
import android.util.JsonWriter
import android.util.SparseArray
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import info.anodsplace.carwidget.content.db.Database
import info.anodsplace.carwidget.content.db.Favorites
import info.anodsplace.carwidget.content.db.FolderItem
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutWithFolderItems
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.shortcuts.ShortcutExtra
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.StringReader
import java.io.StringWriter

@RunWith(AndroidJUnit4::class)
class FolderBackupRestoreTest {

    @Test
    fun folder_shortcuts_backup_and_restore_roundtrip() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "test-folder-backup-restore.db"
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
                itemTypeAdapter = IntColumnAdapter,
            )
        )
        val shortcutsDb = ShortcutsDatabase(database)

        val component = ComponentName(context.packageName, "TestActivity")
        val bmp = Bitmap.createBitmap(1, 1, Config.ARGB_8888)

        // Create folder shortcut (position 0)
        val folderIntent = Intent(ShortcutExtra.ACTION_FOLDER)
        val folderShortcut = Shortcut(
            id = 0,
            position = 0,
            itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION, // itemType arbitrary; folder detected by intent.action
            title = "Folder",
            isCustomIcon = false,
            intent = folderIntent
        )
        val folderItems = listOf(
            Shortcut.forActivity(0, 0, "Child1", false, component, 0),
            Shortcut.forActivity(0, 0, "Child2", false, component, 0)
        )
        val folderItemsWithIcons = folderItems.map { it to ShortcutIcon.forActivity(0, bmp) }

        val folderId = shortcutsDb.saveFolder(ORIGINAL_TARGET, 0, folderShortcut, ShortcutIcon.forActivity(0, bmp), folderItemsWithIcons)
        // Validate pre-backup folder items
        val preFolderItems = shortcutsDb.loadFolderItems(folderId)
        assertEquals(2, preFolderItems.size)

        // Add an app shortcut after folder (position 1)
        val afterShortcut = Shortcut.forActivity(0, 1, "After", false, component, 0)
        shortcutsDb.addItem(ORIGINAL_TARGET, 1, afterShortcut, ShortcutIcon.forActivity(0, bmp))

        val originalMap = shortcutsDb.loadTarget(ORIGINAL_TARGET)
        assertEquals(2, originalMap.size) // positions 0..1

        // Backup JSON
        val stringWriter = StringWriter()
        val writer = JsonWriter(stringWriter)
        writer.beginObject()
        writer.name("settings").beginObject().name("first-time").value(true).endObject()
        writer.name("incar").beginObject().endObject()
        writer.name("shortcuts").beginArray()
        writer.writeShortcuts(originalMap, shortcutsDb, context)
        writer.endArray()
        writer.name("notificationShortcuts").beginArray().endArray()
        writer.endObject()
        writer.close()

        val json = stringWriter.toString()

        // Read back shortcuts (with folder) using reader
        val shortcutsJsonReader = ShortcutsJsonReader(context)
        val reader = JsonReader(StringReader(json))
        var readShortcuts = SparseArray<ShortcutWithFolderItems>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "shortcuts" -> readShortcuts = runBlocking { shortcutsJsonReader.readList(reader) }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        reader.close()

        // Restore into new target
        shortcutsDb.restoreTarget(RESTORE_TARGET, readShortcuts)
        val restoredMap = shortcutsDb.loadTarget(RESTORE_TARGET)
        assertEquals(2, restoredMap.size)

        val restoredFolder = restoredMap[0]
        require(restoredFolder != null && restoredFolder.isFolder) { "Restored folder missing at position 0" }
        val restoredFolderItems = shortcutsDb.loadFolderItems(restoredFolder.id)
        assertEquals(2, restoredFolderItems.size)
        assertEquals("Child1", restoredFolderItems[0].title)
        assertEquals("Child2", restoredFolderItems[1].title)

        val restoredAfter = restoredMap[1]
        assertEquals("After", restoredAfter?.title)
    }

    companion object {
        private const val ORIGINAL_TARGET = 21
        private const val RESTORE_TARGET = 31
    }
}

